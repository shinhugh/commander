package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.repository.AccountRepository;
import org.dev.commander.repository.SessionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.lang.System.currentTimeMillis;

// TODO: Handle concurrency
@Service
public class AccountManager implements AccountService, SessionService, AuthenticationService, AuthorityVerificationService {
    private static final long SESSION_DURATION = 86400000L;
    private static final int SESSION_TOKEN_LENGTH = 64;
    private static final String SESSION_TOKEN_ALLOWED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int LOGIN_NAME_LENGTH_MIN = 4;
    private static final int LOGIN_NAME_LENGTH_MAX = 16;
    private static final String LOGIN_NAME_ALLOWED_CHARS = "-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    private static final int PASSWORD_LENGTH_MIN = 8;
    private static final int PASSWORD_LENGTH_MAX = 32;
    private static final String PASSWORD_ALLOWED_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private static final int PUBLIC_NAME_LENGTH_MIN = 2;
    private static final int PUBLIC_NAME_LENGTH_MAX = 16;
    private static final String PUBLIC_NAME_ALLOWED_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountManager(AccountRepository accountRepository, SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Account readAccountById(Authentication authentication, long id) throws BadRequestException {
        if (id <= 0) {
            throw new BadRequestException();
        }
        Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);
        account.setPassword(null);
        if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
            account.setLoginName(null);
            account.setAuthorities(0);
        }
        return account;
    }

    @Override
    public Account createAccount(Authentication authentication, Account account) throws BadRequestException, ConflictException {
        if (!validateAccount(account, false)) {
            throw new BadRequestException();
        }
        account = deepCopyAccount(account);
        account.setId(0);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setAuthorities(1);
        try {
            account = accountRepository.save(account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        account.setPassword(null);
        return account;
    }

    @Override
    // TODO: Strip password from return value but not persisted value
    // TODO: Use manual transaction (not @Transactional)
    public Account updateAccountById(Authentication authentication, long id, Account account) throws UnauthorizedException, BadRequestException, ForbiddenException, ConflictException {
        if (authentication == null) {
            throw new UnauthorizedException();
        }
        if (id <= 0 || !validateAccount(account, true)) {
            throw new BadRequestException();
        }
        Account existingAccount = accountRepository.findById(id).orElseThrow(NotFoundException::new);
        if (!verifyClientIsOwnerOrAdmin(authentication, existingAccount)) {
            throw new ForbiddenException();
        }

        existingAccount.setLoginName(account.getLoginName());
        existingAccount.setPassword(passwordEncoder.encode(account.getPassword()));
        existingAccount.setAuthorities(account.getAuthorities());
        existingAccount.setPublicName(account.getPublicName());
        sessionRepository.deleteByAccountId(existingAccount.getId());
        return existingAccount;

//        account = deepCopyAccount(account);
//        account.setId(id);
//        account.setPassword(passwordEncoder.encode(account.getPassword()));
//        try {
//            account = accountRepository.save(account);
//        }
//        catch (DataIntegrityViolationException ex) {
//            throw new ConflictException();
//        }
//        sessionRepository.deleteByAccountId(account.getId());
//        account.setPassword(null);
//        return account;
    }

    @Override
    // TODO: Use manual transaction (not @Transactional)
    public void deleteAccountById(Authentication authentication, long id) throws UnauthorizedException, BadRequestException, ForbiddenException {
        if (authentication == null) {
            throw new UnauthorizedException();
        }
        if (id <= 0) {
            throw new BadRequestException();
        }
        Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);
        if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
            throw new ForbiddenException();
        }
        accountRepository.deleteById(id);
        sessionRepository.deleteByAccountId(id);
    }

    @Override
    public String login(Authentication authentication, Credentials credentials) throws BadRequestException, UnauthorizedException {
        if (authentication != null) {
            return (String) authentication.getCredentials();
        }
        if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
            throw new BadRequestException();
        }
        Account account = accountRepository.findByLoginName(credentials.getUsername()).orElseThrow(UnauthorizedException::new);
        if (!passwordEncoder.matches(credentials.getPassword(), account.getPassword())) {
            throw new UnauthorizedException();
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

    @Override
    public void logout(Authentication authentication, boolean all) {
        if (authentication == null) {
            return;
        }
        if (all) {
            sessionRepository.deleteByAccountId(((Account) authentication.getPrincipal()).getId());
        } else {
            sessionRepository.deleteById((String) authentication.getCredentials());
        }
    }

    @Override
    public Session getSession(String token) throws NotFoundException {
        Session session = sessionRepository.findById(token).orElseThrow(NotFoundException::new);
        if (session.getExpirationTime() <= currentTimeMillis()) {
            sessionRepository.deleteById(token);
            throw new NotFoundException();
        }
        return session;
    }

    @Override
    public Account getSessionOwner(Session session) throws NotFoundException {
        return accountRepository.findById(session.getAccountId()).orElseThrow(NotFoundException::new);
    }

    @Override
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

    private boolean verifyClientIsOwnerOrAdmin(Authentication authentication, Account account) {
        if (authentication == null) {
            return false;
        }
        if (account.getLoginName().equals(authentication.getName())) {
            return true;
        }
        return verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"));
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
