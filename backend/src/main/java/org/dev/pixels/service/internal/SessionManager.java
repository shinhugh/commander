package org.dev.pixels.service.internal;

import org.dev.pixels.model.Account;
import org.dev.pixels.model.Credentials;
import org.dev.pixels.model.Session;
import org.dev.pixels.repository.SessionRepository;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotAuthenticatedException;
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
    private static final long PURGE_INTERVAL = 30000;
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
        ChangesAndReturnValue<Session> changes = inner.login(credentials);
        return handleChanges(changes);
    }

    @Override
    public void logout(String token, Long accountId) {
        ChangesAndReturnValue<Void> changes = inner.logout(token, accountId);
        handleChanges(changes);
    }

    @Override
    public void registerSessionEventHandler(SessionEventHandler sessionEventHandler) {
        sessionEventHandlers.add(sessionEventHandler);
    }

    @Override
    public void handleCreateAccount(Account newAccount) { }

    @Override
    public void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) {
        ChangesAndReturnValue<Void> changes = inner.handleUpdateAccount(preUpdateAccount, postUpdateAccount);
        handleChanges(changes);
    }

    @Override
    public void handleDeleteAccount(Account deleteAccount) {
        ChangesAndReturnValue<Void> changes = inner.handleDeleteAccount(deleteAccount);
        handleChanges(changes);
    }

    @Scheduled(fixedRate = PURGE_INTERVAL)
    public void purgeExpiredSessions() {
        ChangesAndReturnValue<Void> changes = inner.purgeExpiredSessions();
        handleChanges(changes);
    }

    private <T> T handleChanges(ChangesAndReturnValue<T> changes) {
        if (changes.getCreatedSessions() != null) {
            for (Session createdSession : changes.getCreatedSessions()) {
                for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                    sessionEventHandler.handleLogin(createdSession);
                }
            }
        }
        if (changes.getDeletedSessions() != null) {
            for (Session deletedSession : changes.getDeletedSessions()) {
                for (SessionEventHandler sessionEventHandler : sessionEventHandlers) {
                    sessionEventHandler.handleLogout(deletedSession);
                }
            }
        }
        return changes.getReturnValue();
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static class Inner {
        private static final int SESSION_TOKEN_LENGTH = 128;
        private static final String SESSION_TOKEN_ALLOWED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static final long SESSION_DURATION = 86400000L;
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
                }
                else {
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

        public ChangesAndReturnValue<Session> login(Credentials credentials) {
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
            session = sessionRepository.save(session);
            return new ChangesAndReturnValue<>(session, Set.of(session), null);
        }

        public ChangesAndReturnValue<Void> logout(String token, Long accountId) {
            Set<Session> sessions = null;
            if (token != null && token.length() > 0) {
                sessions = new HashSet<>();
                Session session = sessionRepository.findById(token).orElse(null);
                if (session != null) {
                    sessions.add(session);
                }
            }
            if (accountId != null && accountId > 0) {
                if (sessions == null) {
                    sessions = new HashSet<>(sessionRepository.findByAccountId(accountId));
                }
                else {
                    sessions = sessions.stream().filter(s -> Objects.equals(s.getAccountId(), accountId)).collect(Collectors.toSet());
                }
            }
            if (sessions == null) {
                throw new IllegalArgumentException();
            }
            for (Session session : sessions) {
                sessionRepository.delete(session);
            }
            return new ChangesAndReturnValue<>(null, null, sessions);
        }

        public ChangesAndReturnValue<Void> handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) {
            Set<Session> sessions = new HashSet<>(sessionRepository.findByAccountId(preUpdateAccount.getId()));
            sessionRepository.deleteByAccountId(preUpdateAccount.getId());
            return new ChangesAndReturnValue<>(null, null, sessions);
        }

        public ChangesAndReturnValue<Void> handleDeleteAccount(Account deletedAccount) {
            Set<Session> sessions = new HashSet<>(sessionRepository.findByAccountId(deletedAccount.getId()));
            sessionRepository.deleteByAccountId(deletedAccount.getId());
            return new ChangesAndReturnValue<>(null, null, sessions);
        }

        public ChangesAndReturnValue<Void> purgeExpiredSessions() {
            long currentTime = currentTimeMillis();
            Set<Session> sessions = new HashSet<>(sessionRepository.findByExpirationTimeLessThanEqual(currentTime));
            sessionRepository.deleteByExpirationTimeLessThanEqual(currentTime);
            return new ChangesAndReturnValue<>(null, null, sessions);
        }

        private String generateToken() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < SESSION_TOKEN_LENGTH; i++) {
                result.append(SESSION_TOKEN_ALLOWED_CHARS.charAt((int) (Math.random() * SESSION_TOKEN_ALLOWED_CHARS.length())));
            }
            return result.toString();
        }
    }

    private static class ChangesAndReturnValue<T> {
        private final T returnValue;
        private final Set<Session> createdSessions;
        private final Set<Session> deletedSessions;

        public ChangesAndReturnValue(T returnValue, Set<Session> createdSessions, Set<Session> deletedSessions) {
            this.returnValue = returnValue;
            this.createdSessions = createdSessions;
            this.deletedSessions = deletedSessions;
        }

        public T getReturnValue() {
            return returnValue;
        }

        public Set<Session> getCreatedSessions() {
            return createdSessions;
        }

        public Set<Session> getDeletedSessions() {
            return deletedSessions;
        }
    }
}
