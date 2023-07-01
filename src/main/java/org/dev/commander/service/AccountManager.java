package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.repository.AccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Handle concurrency
@Service
public class AccountManager implements AccountService, UserDetailsService, AuthorityVerificationService, AuthenticationService {
    private static final List<GrantedAuthority> AUTHORITIES_IN_ORDER = Arrays.asList(
            new SimpleGrantedAuthority("USER"),
            new SimpleGrantedAuthority("ADMIN")
    );
    private static final int LOGIN_NAME_LENGTH_MIN = 5;
    private static final int PASSWORD_LENGTH_MIN = 8;
    private static final int PUBLIC_NAME_LENGTH_MIN = 2;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountManager(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Account readAccountById(Authentication authentication, long id) {
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
    public Account createAccount(Authentication authentication, Account account) {
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
    public Account updateAccountById(Authentication authentication, long id, Account account) {
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
        account = deepCopyAccount(account);
        account.setId(id);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        try {
            account = accountRepository.save(account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        // TODO: Purge all sessions for this account
        account.setPassword(null);
        return account;
    }

    @Override
    public void deleteAccountById(Authentication authentication, long id) {
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
        // TODO: Purge all sessions for this account
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByLoginName(username).orElse(null);
        if (account == null) {
            throw new UsernameNotFoundException(String.format("No user found with username: %s", username));
        }
        Set<GrantedAuthority> authorities = translateAuthoritiesFlagToSet(account.getAuthorities());
        return User.builder().username(account.getLoginName()).password(account.getPassword()).authorities(authorities).build();
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

    @Override
    public String login(Authentication authentication, Credentials credentials) {
        // TODO
        return null;
    }

    @Override
    public void logout(Authentication authentication, boolean all) {
        // TODO
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
        if (loginName == null || loginName.length() < LOGIN_NAME_LENGTH_MIN) {
            return false;
        }
        if (password == null || password.length() < PASSWORD_LENGTH_MIN) {
            return false;
        }
        if (publicName == null || publicName.length() < PUBLIC_NAME_LENGTH_MIN) {
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

    private Set<GrantedAuthority> translateAuthoritiesFlagToSet(int flag) {
        Set<GrantedAuthority> set = new HashSet<>();
        int order = 0;
        while (flag > 0 && order < AUTHORITIES_IN_ORDER.size()) {
            if (flag % 2 > 0) {
                set.add(AUTHORITIES_IN_ORDER.get(order));
            }
            flag /= 2;
            order++;
        }
        return set;
    }
}
