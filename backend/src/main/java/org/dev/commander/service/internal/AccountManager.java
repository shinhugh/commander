package org.dev.commander.service.internal;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Account;
import org.dev.commander.repository.AccountRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountManager implements AccountService {
    private final Inner inner;
    private final Set<AccountEventHandler> accountEventHandlers = new HashSet<>();

    public AccountManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public List<Account> readAccounts(Long id, String loginName) throws IllegalArgumentException {
        return inner.readAccounts(id, loginName);
    }

    @Override
    public Account createAccount(Account account) throws IllegalArgumentException, ConflictException {
        Account newAccount;
        try {
            newAccount = inner.createAccount(account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        // TODO: If external transactional service calls this method, account may have not been created yet, but handlers will be called
        for (AccountEventHandler accountEventHandler : accountEventHandlers) {
            accountEventHandler.handleCreateAccount(newAccount);
        }
        return newAccount;
    }

    @Override
    public Account updateAccount(long id, Account account) throws IllegalArgumentException, NotFoundException, ConflictException {
        AccountUpdate accountUpdate;
        try {
            accountUpdate = inner.updateAccount(id, account);
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException();
        }
        Account preUpdateAccount = accountUpdate.getPreUpdateAccount();
        Account postUpdateAccount = accountUpdate.getPostUpdateAccount();
        // TODO: If external transactional service calls this method, account may have not been updated yet, but handlers will be called
        for (AccountEventHandler accountEventHandler : accountEventHandlers) {
            accountEventHandler.handleUpdateAccount(preUpdateAccount, postUpdateAccount);
        }
        return postUpdateAccount;
    }

    @Override
    public void deleteAccount(long id) throws IllegalArgumentException, NotFoundException {
        Account deletedAccount = inner.deleteAccount(id);
        // TODO: If external transactional service calls this method, account may have not been deleted yet, but handlers will be called
        for (AccountEventHandler accountEventHandler : accountEventHandlers) {
            accountEventHandler.handleDeleteAccount(deletedAccount);
        }
    }

    @Override
    public void registerAccountEventHandler(AccountEventHandler accountEventHandler) {
        accountEventHandlers.add(accountEventHandler);
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
        private static final int AUTHORITIES_MAX = 3;
        private static final int USER_AUTHORITY_ORDER = 0;
        private static final int PUBLIC_NAME_LENGTH_MIN = 2;
        private static final int PUBLIC_NAME_LENGTH_MAX = 16;
        private static final String PUBLIC_NAME_ALLOWED_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        private final AccountRepository accountRepository;
        private final PasswordEncoder passwordEncoder;

        public Inner(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
            this.accountRepository = accountRepository;
            this.passwordEncoder = passwordEncoder;
        }

        public List<Account> readAccounts(Long id, String loginName) {
            List<Account> accounts = null;
            if (id != null && id > 0) {
                Account account = accountRepository.findById(id).orElse(null);
                if (account == null) {
                    return List.of();
                }
                accounts = new ArrayList<>();
                accounts.add(account);
            }
            if (loginName != null && loginName.length() > 0) {
                if (accounts == null) {
                    Account account = accountRepository.findByLoginName(loginName).orElse(null);
                    if (account == null) {
                        return List.of();
                    }
                    accounts = new ArrayList<>();
                    accounts.add(account);
                } else {
                    accounts = accounts.stream().filter(a -> Objects.equals(a.getLoginName(), loginName)).collect(Collectors.toList());
                }
            }
            if (accounts == null) {
                throw new IllegalArgumentException();
            }
            return accounts;
        }

        public Account createAccount(Account account) {
            account = cloneAccount(account);
            int authorities = (int) Math.pow(2, USER_AUTHORITY_ORDER);
            account.setAuthorities(authorities);
            if (!validateAccount(account)) {
                throw new IllegalArgumentException();
            }
            account.setId(null);
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            return accountRepository.save(account);
        }

        public AccountUpdate updateAccount(long id, Account account) {
            account = cloneAccount(account);
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account existingAccount = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            if (!validateAccount(account)) {
                throw new IllegalArgumentException();
            }
            Account oldAccount = cloneAccount(existingAccount);
            existingAccount.setLoginName(account.getLoginName());
            existingAccount.setPassword(passwordEncoder.encode(account.getPassword()));
            existingAccount.setAuthorities(account.getAuthorities());
            existingAccount.setPublicName(account.getPublicName());
            return new AccountUpdate(oldAccount, existingAccount);
        }

        public Account deleteAccount(long id) {
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            accountRepository.delete(account);
            return account;
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

        private boolean validateAccount(Account account) {
            String loginName = account.getLoginName();
            String password = account.getPassword();
            Integer authorities = account.getAuthorities();
            String publicName = account.getPublicName();
            if (loginName == null || loginName.length() < LOGIN_NAME_LENGTH_MIN || loginName.length() > LOGIN_NAME_LENGTH_MAX || !verifyAllowedChars(loginName, LOGIN_NAME_ALLOWED_CHARS)) {
                return false;
            }
            if (password == null || password.length() < PASSWORD_LENGTH_MIN || password.length() > PASSWORD_LENGTH_MAX || !verifyAllowedChars(password, PASSWORD_ALLOWED_CHARS)) {
                return false;
            }
            if (authorities == null || (authorities >> USER_AUTHORITY_ORDER) % 2 != 1 || authorities > AUTHORITIES_MAX) {
                return false;
            }
            if (publicName == null || publicName.length() < PUBLIC_NAME_LENGTH_MIN || publicName.length() > PUBLIC_NAME_LENGTH_MAX || !verifyAllowedChars(publicName, PUBLIC_NAME_ALLOWED_CHARS)) {
                return false;
            }
            return true;
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

    private static class AccountUpdate {
        private final Account preUpdateAccount;
        private final Account postUpdateAccount;

        public AccountUpdate(Account preUpdateAccount, Account postUpdateAccount) {
            this.preUpdateAccount = preUpdateAccount;
            this.postUpdateAccount = postUpdateAccount;
        }

        public Account getPreUpdateAccount() {
            return preUpdateAccount;
        }

        public Account getPostUpdateAccount() {
            return postUpdateAccount;
        }
    }
}
