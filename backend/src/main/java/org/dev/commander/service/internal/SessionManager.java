package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.repository.SessionRepository;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public Session login(Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        Session session = inner.login(credentials);
        for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
            sessionEventHandler.handleLogin(session);
        }
        return session;
    }

    @Override
    public void logout(String token, Long accountId) {
        List<Session> deletedSessions = inner.logout(token, accountId);
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleLogout(deletedSession);
            }
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
                sessionEventHandler.handleLogout(deletedSession);
            }
        }
    }

    @Override
    public void handleDeleteAccount(Account deleteAccount) {
        List<Session> deletedSessions = inner.handleDeleteAccount(deleteAccount);
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleLogout(deletedSession);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpiredSessions() {
        List<Session> deletedSessions = inner.purgeExpiredSessions();
        for (Session deletedSession : deletedSessions) {
            for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                sessionEventHandler.handleLogout(deletedSession);
            }
        }
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static class Inner {
        private static final int SESSION_TOKEN_LENGTH = 128;
        private static final String SESSION_TOKEN_ALLOWED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static final long SESSION_DURATION = 86400000L;
        private static final int AUTHORITIES_MAX = 3;
        private static final int USER_AUTHORITY_ORDER = 0;
        private final SessionRepository sessionRepository;
        private final AccountService accountService;
        private final PasswordEncoder passwordEncoder;

        public Inner(SessionRepository sessionRepository, AccountService accountService, PasswordEncoder passwordEncoder) {
            this.sessionRepository = sessionRepository;
            this.accountService = accountService;
            this.passwordEncoder = passwordEncoder;
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

        public Session login(Credentials credentials) {
            if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
                throw new IllegalArgumentException();
            }
            List<Account> accounts = accountService.readAccounts(null, credentials.getUsername());
            if (accounts.isEmpty()) {
                throw new NotAuthenticatedException();
            }
            Account account = accounts.get(0);
            if (!passwordEncoder.matches(credentials.getPassword(), account.getPassword())) {
                throw new NotAuthenticatedException();
            }
            String token;
            do {
                token = generateToken();
            } while (sessionRepository.existsById(token));
            long creationTime = currentTimeMillis();
            long expirationTime = creationTime + SESSION_DURATION;
            Session session = new Session();
            session.setToken(token);
            session.setAccountId(account.getId());
            session.setAuthorities(account.getAuthorities());
            session.setCreationTime(creationTime);
            session.setExpirationTime(expirationTime);
            return sessionRepository.save(session);
        }

        public List<Session> logout(String token, Long accountId) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
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
}
