package org.dev.commander.service.internal;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Account;
import org.dev.commander.model.Session;
import org.dev.commander.repository.SessionRepository;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

@Service
public class SessionManager implements SessionService, AccountEventHandler {
    private final Inner inner;
    private final Set<SessionEventHandler> sessionEventHandlers = new HashSet<>();

    public SessionManager(Inner inner, AccountService accountService) {
        this.inner = inner;
        accountService.registerAccountEventHandler(this);
    }

    @Override
    public List<Session> readSessions(String token, Long accountId) throws IllegalArgumentException {
        return inner.readSessions(token, accountId);
    }

    @Override
    public Session createSession(Session session) throws IllegalArgumentException {
        Session newSession = inner.createSession(session);
        // TODO: If external transactional service calls this method, session may have not been created yet, but handlers will be called
        for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
            sessionEventHandler.handleCreateSession(newSession);
        }
        return newSession;
    }

    @Override
    public Session updateSession(String token, Session session) throws IllegalArgumentException, NotFoundException {
        SessionUpdate sessionUpdate = inner.updateSession(token, session);
        Session preUpdateSession = sessionUpdate.getPreUpdateSession();
        Session postUpdateSession = sessionUpdate.getPostUpdateSession();
        // TODO: If external transactional service calls this method, session may have not been updated yet, but handlers will be called
        for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
            sessionEventHandler.handleUpdateSession(preUpdateSession, postUpdateSession);
        }
        return postUpdateSession;
    }

    @Override
    public void deleteSession(String token) throws IllegalArgumentException, NotFoundException {
        Session deletedSession = inner.deleteSession(token);
        // TODO: If external transactional service calls this method, session may have not been deleted yet, but handlers will be called
        for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
            sessionEventHandler.handleDeleteSession(deletedSession);
        }
    }

    @Override
    public void registerSessionEventHandler(SessionEventHandler sessionEventHandler) {
        sessionEventHandlers.add(sessionEventHandler);
    }

    @Override
    public void handleCreateAccount(Account newAccount) { }

    @Override
    public void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) {
        List<Session> deletedSessions = inner.handleUpdateAccount(preUpdateAccount, postUpdateAccount);
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleDeleteSession(deletedSession);
            }
        }
    }

    @Override
    public void handleDeleteAccount(Account deleteAccount) {
        List<Session> deletedSessions = inner.handleDeleteAccount(deleteAccount);
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleDeleteSession(deletedSession);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpiredSessions() {
        List<Session> deletedSessions = inner.purgeExpiredSessions();
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleDeleteSession(deletedSession);
            }
        }
    }

    @Component
    @Transactional
    public static class Inner {
        private static final int SESSION_TOKEN_LENGTH = 128;
        private static final String SESSION_TOKEN_ALLOWED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static final long SESSION_DURATION = 86400000L;
        private static final int AUTHORITIES_MAX = 3;
        private static final int USER_AUTHORITY_ORDER = 0;
        private final SessionRepository sessionRepository;

        public Inner(SessionRepository sessionRepository) {
            this.sessionRepository = sessionRepository;
        }

        public List<Session> readSessions(String token, Long accountId) {
            List<Session> sessions = null;
            if (token != null && token.length() > 0) {
                Session session = sessionRepository.findById(token).orElse(null);
                if (session == null) {
                    return List.of();
                }
                sessions = new ArrayList<>();
                sessions.add(session);
            }
            if (accountId != null && accountId > 0) {
                if (sessions == null) {
                    sessions = new ArrayList<>(sessionRepository.findByAccountId(accountId));
                } else {
                    sessions = sessions.stream().filter(s -> Objects.equals(s.getAccountId(), accountId)).collect(Collectors.toList());
                }
            }
            if (sessions == null) {
                throw new IllegalArgumentException();
            }
            long currentTime = currentTimeMillis();
            sessions.removeIf(s -> s.getExpirationTime() <= currentTime);
            return sessions;
        }

        public Session createSession(Session session) {
            session = cloneSession(session);
            long creationTime = currentTimeMillis();
            long expirationTime = creationTime + SESSION_DURATION;
            session.setCreationTime(creationTime);
            session.setExpirationTime(expirationTime);
            if (!validateSession(session)) {
                throw new IllegalArgumentException();
            }
            String token;
            do {
                token = generateToken();
            } while (sessionRepository.existsById(token));
            session.setToken(token);
            return sessionRepository.save(session);
        }

        public SessionUpdate updateSession(String token, Session session) {
            session = cloneSession(session);
            if (token == null || token.length() == 0) {
                throw new IllegalArgumentException();
            }
            Session existingSession = sessionRepository.findById(token).orElseThrow(NotFoundException::new);
            session.setAccountId(existingSession.getAccountId());
            session.setAuthorities(existingSession.getAuthorities());
            session.setCreationTime(existingSession.getCreationTime());
            if (!validateSession(session)) {
                throw new IllegalArgumentException();
            }
            Session oldSession = cloneSession(existingSession);
            existingSession.setExpirationTime(session.getExpirationTime());
            return new SessionUpdate(oldSession, existingSession);
        }

        public Session deleteSession(String token) {
            if (token == null || token.length() == 0) {
                throw new IllegalArgumentException();
            }
            Session session = sessionRepository.findById(token).orElseThrow(NotFoundException::new);
            sessionRepository.delete(session);
            if (session.getExpirationTime() <= currentTimeMillis()) {
                throw new NotFoundException();
            }
            return session;
        }

        public List<Session> handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) {
            List<Session> sessions = sessionRepository.findByAccountId(preUpdateAccount.getId());
            sessionRepository.deleteByAccountId(preUpdateAccount.getId());
            return sessions;
        }

        public List<Session> handleDeleteAccount(Account deletedAccount) {
            List<Session> sessions = sessionRepository.findByAccountId(deletedAccount.getId());
            sessionRepository.deleteByAccountId(deletedAccount.getId());
            return sessions;
        }

        public List<Session> purgeExpiredSessions() {
            long currentTime = currentTimeMillis();
            List<Session> expiredSessions = sessionRepository.findByExpirationTimeLessThanEqual(currentTime);
            sessionRepository.deleteByExpirationTimeLessThanEqual(currentTime);
            return expiredSessions;
        }

        private Session cloneSession(Session session) {
            Session clone = new Session();
            clone.setToken(session.getToken());
            clone.setAccountId(session.getAccountId());
            clone.setAuthorities(session.getAuthorities());
            clone.setCreationTime(session.getCreationTime());
            clone.setExpirationTime(session.getExpirationTime());
            return clone;
        }

        private boolean validateSession(Session session) {
            Long accountId = session.getAccountId();
            Integer authorities = session.getAuthorities();
            Long creationTime = session.getCreationTime();
            Long expirationTime = session.getExpirationTime();
            if (accountId == null || accountId <= 0) {
                return false;
            }
            if (authorities == null || (authorities >> USER_AUTHORITY_ORDER) % 2 != 1 || authorities > AUTHORITIES_MAX) {
                return false;
            }
            if (creationTime == null || creationTime < 0) {
                return false;
            }
            if (expirationTime == null || expirationTime < 0 || expirationTime < creationTime) {
                return false;
            }
            return true;
        }

        private String generateToken() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < SESSION_TOKEN_LENGTH; i++) {
                result.append(SESSION_TOKEN_ALLOWED_CHARS.charAt((int) (Math.random() * SESSION_TOKEN_ALLOWED_CHARS.length())));
            }
            return result.toString();
        }
    }

    private static class SessionUpdate {
        private final Session preUpdateSession;
        private final Session postUpdateSession;

        public SessionUpdate(Session preUpdateAccount, Session postUpdateAccount) {
            this.preUpdateSession = preUpdateAccount;
            this.postUpdateSession = postUpdateAccount;
        }

        public Session getPreUpdateSession() {
            return preUpdateSession;
        }

        public Session getPostUpdateSession() {
            return postUpdateSession;
        }
    }
}
