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

import java.util.List;
import java.util.Set;

import static java.lang.System.currentTimeMillis;

@Service
public class AuthenticationManager implements AuthenticationService, AuthorizationService, AccountService {
    private final Inner inner;

    public AuthenticationManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public Session identifySession(String token) throws NotFoundException {
        return inner.identifySession(token);
    }

    @Override
    public Account identifyAccount(String token) throws NotFoundException {
        return inner.identifyAccount(token);
    }

    @Override
    public Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        return inner.login(authentication, credentials);
    }

    @Override
    public void logout(Authentication authentication, Boolean all) {
        inner.logout(authentication, all);
    }

    @Override
    public Account getAccount(Authentication authentication) {
        return inner.getAccount(authentication);
    }

    @Override
    public boolean verifyAuthenticationContainsAtLeastOneAuthority(Authentication authentication, Set<String> authorities) {
        return inner.verifyAuthenticationContainsAtLeastOneAuthority(authentication, authorities);
    }

    @Override
    public Account readAccount(Authentication authentication, Long id) throws IllegalArgumentException, NotFoundException {
        return inner.readAccount(authentication, id);
    }

    @Override
    public Account createAccount(Authentication authentication, Account account) throws IllegalArgumentException, ConflictException {
        try {
            return inner.createAccount(account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
    }

    @Override
    public Account updateAccount(Authentication authentication, Long id, Account account) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException, ConflictException {
        try {
            return inner.updateAccount(authentication, id, account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
    }

    @Override
    public void deleteAccount(Authentication authentication, Long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException {
        inner.deleteAccount(authentication, id);
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpiredSessions() {
        inner.purgeExpiredSessions();
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
        private final SessionRepository sessionRepository;
        private final AccountRepository accountRepository;
        private final WebSocketRegistrar webSocketRegistrar;
        private final PasswordEncoder passwordEncoder;

        public Inner(SessionRepository sessionRepository, AccountRepository accountRepository, WebSocketRegistrar webSocketRegistrar, PasswordEncoder passwordEncoder) {
            this.sessionRepository = sessionRepository;
            this.accountRepository = accountRepository;
            this.webSocketRegistrar = webSocketRegistrar;
            this.passwordEncoder = passwordEncoder;
        }

        public Session identifySession(String token) {
            Session session = sessionRepository.findById(token).orElseThrow(NotFoundException::new);
            if (session.getExpirationTime() <= currentTimeMillis()) {
                sessionRepository.deleteById(token);
                throw new NotFoundException();
            }
            return cloneSession(session);
        }

        public Account identifyAccount(String token) {
            Session session = identifySession(token);
            return cloneAccount(accountRepository.findById(session.getAccountId()).orElseThrow(NotFoundException::new));
        }

        public Session login(Authentication authentication, Credentials credentials) {
            if (authentication != null) {
                Session session = sessionRepository.findById((String) authentication.getCredentials()).orElse(null);
                if (session != null) {
                    return session;
                }
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
            session = cloneSession(session);
            session.setAccountId(null);
            session.setAuthorities(null);
            return session;
        }

        public void logout(Authentication authentication, Boolean all) {
            if (authentication == null) {
                return;
            }
            if (all != null && all) {
                long accountId = ((Account) authentication.getPrincipal()).getId();
                sessionRepository.deleteByAccountId(accountId);
                webSocketRegistrar.closeConnectionsForAccount(accountId);
            } else {
                String sessionToken = (String) authentication.getCredentials();
                sessionRepository.deleteById(sessionToken);
                webSocketRegistrar.closeConnectionForSession(sessionToken);
            }
        }

        public Account getAccount(Authentication authentication) {
            if (authentication == null) {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal.getClass() != Account.class) {
                return null;
            }
            return (Account) principal;
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

        public Account readAccount(Authentication authentication, Long id) {
            if (id == null) {
                if (authentication == null) {
                    throw new IllegalArgumentException();
                }
                id = ((Account) authentication.getPrincipal()).getId();
            }
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account account = cloneAccount(accountRepository.findById(id).orElseThrow(NotFoundException::new));
            account.setPassword(null);
            if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
                account.setLoginName(null);
                account.setAuthorities(null);
            }
            return account;
        }

        public Account createAccount(Account account) {
            if (!validateAccount(account, false, false)) {
                throw new IllegalArgumentException();
            }
            account = deepCopyAccount(account);
            account.setId(null);
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            account.setAuthorities(1);
            account = cloneAccount(accountRepository.save(account));
            account.setPassword(null);
            return account;
        }

        public Account updateAccount(Authentication authentication, Long id, Account account) {
            if (authentication == null) {
                throw new NotAuthenticatedException();
            }
            if (id == null) {
                id = ((Account) authentication.getPrincipal()).getId();
            }
            if (id <= 0 || !validateAccount(account, true, true)) {
                throw new IllegalArgumentException();
            }
            Account existingAccount = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            if (!verifyClientIsOwnerOrAdmin(authentication, existingAccount)) {
                throw new NotAuthorizedException();
            }
            if (account.getLoginName() != null && !"".equals(account.getLoginName())) {
                existingAccount.setLoginName(account.getLoginName());
            }
            if (account.getPassword() != null && !"".equals(account.getPassword())) {
                existingAccount.setPassword(passwordEncoder.encode(account.getPassword()));
            }
            if (verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN")) && account.getAuthorities() != null && account.getAuthorities() != 0) {
                existingAccount.setAuthorities(account.getAuthorities());
            }
            if (account.getPublicName() != null && !"".equals(account.getPublicName())) {
                existingAccount.setPublicName(account.getPublicName());
            }
            sessionRepository.deleteByAccountId(id);
            webSocketRegistrar.closeConnectionsForAccount(id);
            existingAccount = cloneAccount(existingAccount);
            existingAccount.setPassword(null);
            // TODO: Notify friends via WebSocket
            return existingAccount;
        }

        public void deleteAccount(Authentication authentication, Long id) {
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
            webSocketRegistrar.closeConnectionsForAccount(id);
            // TODO: Terminate affiliated friendships
            sessionRepository.deleteByAccountId(id);
            accountRepository.delete(account);
        }

        public void purgeExpiredSessions() {
            long currentTime = currentTimeMillis();
            List<Session> expiredSessions = sessionRepository.findByExpirationTimeLessThanEqual(currentTime);
            for (Session session : expiredSessions) {
                webSocketRegistrar.closeConnectionForSession(session.getToken());
            }
            sessionRepository.deleteByExpirationTimeLessThanEqual(currentTime);
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

        private boolean validateAccount(Account account, boolean allowBlankFields, boolean validateAuthorities) {
            if (account == null) {
                return false;
            }
            String loginName = account.getLoginName();
            String password = account.getPassword();
            String publicName = account.getPublicName();
            Integer authorities = account.getAuthorities();
            if (!allowBlankFields || (loginName != null && !"".equals(loginName))) {
                if (loginName == null || loginName.length() < LOGIN_NAME_LENGTH_MIN || loginName.length() > LOGIN_NAME_LENGTH_MAX || !verifyAllowedChars(loginName, LOGIN_NAME_ALLOWED_CHARS)) {
                    return false;
                }
            }
            if (!allowBlankFields || (password != null && !"".equals(password))) {
                if (password == null || password.length() < PASSWORD_LENGTH_MIN || password.length() > PASSWORD_LENGTH_MAX || !verifyAllowedChars(password, PASSWORD_ALLOWED_CHARS)) {
                    return false;
                }
            }
            if (!allowBlankFields || (publicName != null && !"".equals(publicName))) {
                if (publicName == null || publicName.length() < PUBLIC_NAME_LENGTH_MIN || publicName.length() > PUBLIC_NAME_LENGTH_MAX || !verifyAllowedChars(publicName, PUBLIC_NAME_ALLOWED_CHARS)) {
                    return false;
                }
            }
            if (validateAuthorities) {
                if (!allowBlankFields || (authorities != null && authorities != 0)) {
                    return authorities != null && authorities % 2 == 1 && authorities <= 3;
                }
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

        private Session cloneSession(Session session) {
            Session clone = new Session();
            clone.setToken(session.getToken());
            clone.setAccountId(session.getAccountId());
            clone.setAuthorities(session.getAuthorities());
            clone.setCreationTime(session.getCreationTime());
            clone.setExpirationTime(session.getExpirationTime());
            return clone;
        }

        private Account cloneAccount(Account account) {
            Account clone = new Account();
            clone.setId(account.getId());
            clone.setLoginName(account.getLoginName());
            clone.setPassword(account.getPassword());
            clone.setAuthorities(account.getAuthorities());
            clone.setPublicName(account.getPublicName());
            return clone;
        }
    }
}
