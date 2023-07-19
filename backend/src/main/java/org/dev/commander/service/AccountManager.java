package org.dev.commander.service;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.repository.AccountRepository;
import org.dev.commander.repository.SessionRepository;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.dev.commander.websocket.WebSocketRegistrar;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.lang.System.currentTimeMillis;

@Service
public class AccountManager implements AccountService, SessionService, AuthenticationService, AuthorityVerificationService {
    private final Inner inner;

    public AccountManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public Account readAccountById(Authentication authentication, Long id) throws IllegalArgumentException, NotFoundException {
        Account account = inner.readAccountById(authentication, id);
        account.setPassword(null);
        if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
            account.setLoginName(null);
            account.setAuthorities(0);
        }
        return account;
    }

    @Override
    public Account createAccount(Authentication authentication, Account account) throws IllegalArgumentException, ConflictException {
        try {
            account = inner.createAccount(account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        account.setPassword(null);
        return account;
    }

    @Override
    public Account updateAccountById(Authentication authentication, Long id, Account account) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException, ConflictException {
        try {
            account = inner.updateAccountById(authentication, id, account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        account.setPassword(null);
        return account;
    }

    @Override
    public void deleteAccountById(Authentication authentication, Long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException {
        inner.deleteAccountById(authentication, id);
    }

    @Override
    public Session getSession(String token) throws NotFoundException {
        return inner.getSession(token);
    }

    @Override
    public Account getSessionOwner(Session session) throws NotFoundException {
        return inner.getSessionOwner(session);
    }

    @Override
    public String login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        return inner.login(authentication, credentials);
    }

    @Override
    public void logout(Authentication authentication, boolean all) {
        inner.logout(authentication, all);
    }

    @Override
    public long getAccountId(Authentication authentication) {
        return inner.getAccountId(authentication);
    }

    @Override
    public boolean verifyAuthenticationContainsAtLeastOneAuthority(Authentication authentication, Set<String> authorities) {
        return inner.verifyAuthenticationContainsAtLeastOneAuthority(authentication, authorities);
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpiredSessions() {
        inner.purgeExpiredSessions();
    }

    private boolean verifyClientIsOwnerOrAdmin(Authentication authentication, Account account) {
        if (authentication == null) {
            return false;
        }
        if (account.getLoginName().equals(authentication.getName())) {
            return true;
        }
        return verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"));
    }

    @Component
    @Transactional
    public static class Inner {
        private static final int LOGIN_NAME_LENGTH_MIN = 4;
        private static final int LOGIN_NAME_LENGTH_MAX = 16;
        private static final String LOGIN_NAME_ALLOWED_CHARS = "-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
        private static final int PASSWORD_LENGTH_MIN = 8;
        private static final int PASSWORD_LENGTH_MAX = 32;
        private static final String PASSWORD_ALLOWED_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        private static final int PUBLIC_NAME_LENGTH_MIN = 2;
        private static final int PUBLIC_NAME_LENGTH_MAX = 16;
        private static final String PUBLIC_NAME_ALLOWED_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        private static final int SESSION_TOKEN_LENGTH = 128;
        private static final String SESSION_TOKEN_ALLOWED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static final long SESSION_DURATION = 86400000L;
        private final AccountRepository accountRepository;
        private final SessionRepository sessionRepository;
        private final WebSocketRegistrar webSocketRegistrar;
        private final PasswordEncoder passwordEncoder;

        public Inner(AccountRepository accountRepository, SessionRepository sessionRepository, WebSocketRegistrar webSocketRegistrar, PasswordEncoder passwordEncoder) {
            this.accountRepository = accountRepository;
            this.sessionRepository = sessionRepository;
            this.webSocketRegistrar = webSocketRegistrar;
            this.passwordEncoder = passwordEncoder;
        }

        public Account readAccountById(Authentication authentication, Long id) {
            if (id == null) {
                if (authentication == null) {
                    throw new IllegalArgumentException();
                }
                id = ((Account) authentication.getPrincipal()).getId();
            }
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            return accountRepository.findById(id).orElseThrow(NotFoundException::new);
        }

        public Account createAccount(Account account) {
            if (!validateAccount(account, false)) {
                throw new IllegalArgumentException();
            }
            account = deepCopyAccount(account);
            account.setId(0);
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            account.setAuthorities(1);
            return accountRepository.save(account);
        }

        // TODO: Interpret null fields in account as "leave unchanged"
        public Account updateAccountById(Authentication authentication, Long id, Account account) {
            if (authentication == null) {
                throw new NotAuthenticatedException();
            }
            if (id == null) {
                id = ((Account) authentication.getPrincipal()).getId();
            }
            if (id <= 0 || !validateAccount(account, account.getAuthorities() != 0)) {
                throw new IllegalArgumentException();
            }
            Account existingAccount = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            if (!verifyClientIsOwnerOrAdmin(authentication, existingAccount)) {
                throw new NotAuthorizedException();
            }
            existingAccount.setLoginName(account.getLoginName());
            existingAccount.setPassword(passwordEncoder.encode(account.getPassword()));
            if (verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN")) && account.getAuthorities() != 0) {
                existingAccount.setAuthorities(account.getAuthorities());
            }
            existingAccount.setPublicName(account.getPublicName());
            sessionRepository.deleteByAccountId(id);
            webSocketRegistrar.closeConnectionsForAccount(id);
            return existingAccount;
        }

        public void deleteAccountById(Authentication authentication, Long id) {
            if (authentication == null) {
                throw new NotAuthenticatedException();
            }
            if (id == null) {
                id = ((Account) authentication.getPrincipal()).getId();
            }
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
                throw new NotAuthorizedException();
            }
            accountRepository.deleteById(id);
            sessionRepository.deleteByAccountId(id);
            webSocketRegistrar.closeConnectionsForAccount(id);
        }

        public Session getSession(String token) {
            Session session = sessionRepository.findById(token).orElseThrow(NotFoundException::new);
            if (session.getExpirationTime() <= currentTimeMillis()) {
                sessionRepository.deleteById(token);
                throw new NotFoundException();
            }
            return session;
        }

        public Account getSessionOwner(Session session) {
            return accountRepository.findById(session.getAccountId()).orElseThrow(NotFoundException::new);
        }

        public String login(Authentication authentication, Credentials credentials) {
            if (authentication != null) {
                return (String) authentication.getCredentials();
            }
            if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
                throw new IllegalArgumentException();
            }
            Account account = accountRepository.findByLoginName(credentials.getUsername()).orElseThrow(NotAuthenticatedException::new);
            if (!passwordEncoder.matches(credentials.getPassword(), account.getPassword())) {
                throw new NotAuthenticatedException();
            }
            String token;
            do {
                token = generateSessionToken();
            } while (sessionRepository.existsById(token));
            long creationTime = currentTimeMillis();
            long expirationTime = creationTime + SESSION_DURATION;
            Session session = new Session();
            session.setToken(token);
            session.setAccountId(account.getId());
            session.setAuthorities(account.getAuthorities());
            session.setCreationTime(creationTime);
            session.setExpirationTime(expirationTime);
            sessionRepository.save(session);
            return token;
        }

        public void logout(Authentication authentication, boolean all) {
            if (authentication == null) {
                return;
            }
            if (all) {
                long accountId = ((Account) authentication.getPrincipal()).getId();
                sessionRepository.deleteByAccountId(accountId);
                webSocketRegistrar.closeConnectionsForAccount(accountId);
            } else {
                String sessionToken = (String) authentication.getCredentials();
                sessionRepository.deleteById(sessionToken);
                webSocketRegistrar.closeConnectionForSession(sessionToken);
            }
        }

        public long getAccountId(Authentication authentication) {
            if (authentication == null) {
                return 0;
            }
            return ((Account) authentication.getPrincipal()).getId();
        }

        public boolean verifyAuthenticationContainsAtLeastOneAuthority(Authentication authentication, Set<String> authorities) {
            if (authentication == null) {
                return false;
            }
            if (authorities == null || authorities.size() == 0) {
                return true;
            }
            return authentication
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> authorities.contains(a.getAuthority()));
        }

        public void purgeExpiredSessions() {
            // TODO: Purge expired sessions and close corresponding WebSocket connections
        }

        private boolean verifyClientIsOwnerOrAdmin(Authentication authentication, Account account) {
            if (authentication == null) {
                return false;
            }
            if (account.getLoginName().equals(authentication.getName())) {
                return true;
            }
            return verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"));
        }

        private Account deepCopyAccount(Account account) {
            Account accountCopy = new Account();
            accountCopy.setId(account.getId());
            accountCopy.setLoginName(account.getLoginName());
            accountCopy.setPassword(account.getPassword());
            accountCopy.setAuthorities(account.getAuthorities());
            accountCopy.setPublicName(account.getPublicName());
            return accountCopy;
        }

        private boolean validateAccount(Account account, boolean validateAuthorities) {
            if (account == null) {
                return false;
            }
            String loginName = account.getLoginName();
            String password = account.getPassword();
            String publicName = account.getPublicName();
            int authorities = account.getAuthorities();
            if (loginName == null || loginName.length() < LOGIN_NAME_LENGTH_MIN || loginName.length() > LOGIN_NAME_LENGTH_MAX || !verifyAllowedChars(loginName, LOGIN_NAME_ALLOWED_CHARS)) {
                return false;
            }
            if (password == null || password.length() < PASSWORD_LENGTH_MIN || password.length() > PASSWORD_LENGTH_MAX || !verifyAllowedChars(password, PASSWORD_ALLOWED_CHARS)) {
                return false;
            }
            if (publicName == null || publicName.length() < PUBLIC_NAME_LENGTH_MIN || publicName.length() > PUBLIC_NAME_LENGTH_MAX || !verifyAllowedChars(publicName, PUBLIC_NAME_ALLOWED_CHARS)) {
                return false;
            }
            if (validateAuthorities) {
                return authorities % 2 == 1 && authorities <= 3;
            }
            return true;
        }

        private String generateSessionToken() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < SESSION_TOKEN_LENGTH; i++) {
                result.append(SESSION_TOKEN_ALLOWED_CHARS.charAt((int) (Math.random() * SESSION_TOKEN_ALLOWED_CHARS.length())));
            }
            return result.toString();
        }

        private boolean verifyAllowedChars(String subject, String allowedChars) {
            for (char c : subject.toCharArray()) {
                if (!allowedChars.contains(String.valueOf(c))) {
                    return false;
                }
            }
            return true;
        }
    }
}
