package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.repository.AccountRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        ChangesAndReturnValue<Account> changes = inner.createAccount(account);
        return handleChanges(changes);
    }

    @Override
    public Account updateAccount(long id, Account account) throws IllegalArgumentException, NotFoundException, ConflictException {
        ChangesAndReturnValue<Account> changes = inner.updateAccount(id, account);
        return handleChanges(changes);
    }

    @Override
    public void deleteAccount(long id) throws IllegalArgumentException, NotFoundException {
        ChangesAndReturnValue<Void> changes = inner.deleteAccount(id);
        handleChanges(changes);
    }

    @Override
    public void registerAccountEventHandler(AccountEventHandler accountEventHandler) {
        accountEventHandlers.add(accountEventHandler);
    }

    private <T> T handleChanges(ChangesAndReturnValue<T> changes) {
        if (changes.getCreatedAccounts() != null) {
            for (Account createdAccount : changes.getCreatedAccounts()) {
                for (AccountEventHandler accountEventHandler : accountEventHandlers) {
                    accountEventHandler.handleCreateAccount(createdAccount);
                }
            }
        }
        if (changes.getUpdatedAccounts() != null) {
            for (Map.Entry<Account, Account> entry : changes.getUpdatedAccounts().entrySet()) {
                for (AccountEventHandler accountEventHandler : accountEventHandlers) {
                    accountEventHandler.handleUpdateAccount(entry.getKey(), entry.getValue());
                }
            }
        }
        if (changes.getDeletedAccounts() != null) {
            for (Account deletedAccount : changes.getDeletedAccounts()) {
                for (AccountEventHandler accountEventHandler : accountEventHandlers) {
                    accountEventHandler.handleDeleteAccount(deletedAccount);
                }
            }
        }
        return changes.getReturnValue();
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
                accounts = new ArrayList<>();
                Account account = accountRepository.findById(id).orElse(null);
                if (account != null) {
                    accounts.add(account);
                }
            }
            if (loginName != null && loginName.length() > 0) {
                if (accounts == null) {
                    accounts = new ArrayList<>();
                    Account account = accountRepository.findByLoginName(loginName).orElse(null);
                    if (account != null) {
                        accounts.add(account);
                    }
                } else {
                    accounts = accounts.stream().filter(a -> Objects.equals(a.getLoginName(), loginName)).collect(Collectors.toList());
                }
            }
            if (accounts == null) {
                throw new IllegalArgumentException();
            }
            return accounts;
        }

        public ChangesAndReturnValue<Account> createAccount(Account account) {
            account = cloneAccount(account);
            int authorities = (int) Math.pow(2, USER_AUTHORITY_ORDER);
            account.setAuthorities(authorities);
            if (!validateAccount(account, false)) {
                throw new IllegalArgumentException();
            }
            if (accountRepository.existsByLoginNameIgnoreCase(account.getLoginName())) {
                throw new ConflictException();
            }
            account.setId(null);
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            account = accountRepository.save(account);
            return new ChangesAndReturnValue<>(account, Set.of(account), null, null);
        }

        public ChangesAndReturnValue<Account> updateAccount(long id, Account account) {
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account existingAccount = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            if (!validateAccount(account, true)) {
                throw new IllegalArgumentException();
            }
            if (account.getLoginName() != null) {
                List<Account> conflictAccounts = accountRepository.findByLoginNameIgnoreCase(account.getLoginName());
                if (!conflictAccounts.isEmpty() && !Objects.equals(conflictAccounts.get(0).getId(), existingAccount.getId())) {
                    throw new ConflictException();
                }
            }
            Account oldAccount = cloneAccount(existingAccount);
            if (account.getLoginName() != null) {
                existingAccount.setLoginName(account.getLoginName());
            }
            if (account.getPassword() != null) {
                existingAccount.setPassword(passwordEncoder.encode(account.getPassword()));
            }
            if (account.getAuthorities() != null) {
                existingAccount.setAuthorities(account.getAuthorities());
            }
            if (account.getPublicName() != null) {
                existingAccount.setPublicName(account.getPublicName());
            }
            return new ChangesAndReturnValue<>(existingAccount, null, Map.of(oldAccount, existingAccount), null);
        }

        public ChangesAndReturnValue<Void> deleteAccount(long id) {
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
            Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);
            accountRepository.delete(account);
            return new ChangesAndReturnValue<>(null, null, null, Set.of(account));
        }

        private Account cloneAccount(Account account) {
            if (account == null) {
                return null;
            }
            Account clone = new Account();
            clone.setId(account.getId());
            clone.setLoginName(account.getLoginName());
            clone.setPassword(account.getPassword());
            clone.setAuthorities(account.getAuthorities());
            clone.setPublicName(account.getPublicName());
            return clone;
        }

        private boolean validateAccount(Account account, boolean allowNullFields) {
            String loginName = account.getLoginName();
            String password = account.getPassword();
            Integer authorities = account.getAuthorities();
            String publicName = account.getPublicName();
            if (!allowNullFields && (loginName == null || password == null || authorities == null || publicName == null)) {
                return false;
            }
            if (loginName != null && (loginName.length() < LOGIN_NAME_LENGTH_MIN || loginName.length() > LOGIN_NAME_LENGTH_MAX || !verifyAllowedChars(loginName, LOGIN_NAME_ALLOWED_CHARS))) {
                return false;
            }
            if (password != null && (password.length() < PASSWORD_LENGTH_MIN || password.length() > PASSWORD_LENGTH_MAX || !verifyAllowedChars(password, PASSWORD_ALLOWED_CHARS))) {
                return false;
            }
            if (authorities != null && ((authorities >> USER_AUTHORITY_ORDER) % 2 != 1 || authorities > AUTHORITIES_MAX)) {
                return false;
            }
            if (publicName != null && (publicName.length() < PUBLIC_NAME_LENGTH_MIN || publicName.length() > PUBLIC_NAME_LENGTH_MAX || !verifyAllowedChars(publicName, PUBLIC_NAME_ALLOWED_CHARS))) {
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

    private static class ChangesAndReturnValue<T> {
        private final T returnValue;
        private final Set<Account> createdAccounts;
        private final Map<Account, Account> updatedAccounts;
        private final Set<Account> deletedAccounts;

        public ChangesAndReturnValue(T returnValue, Set<Account> createdAccounts, Map<Account, Account> updatedAccounts, Set<Account> deletedAccounts) {
            this.returnValue = returnValue;
            this.createdAccounts = createdAccounts;
            this.updatedAccounts = updatedAccounts;
            this.deletedAccounts = deletedAccounts;
        }

        public T getReturnValue() {
            return returnValue;
        }

        public Set<Account> getCreatedAccounts() {
            return createdAccounts;
        }

        public Map<Account, Account> getUpdatedAccounts() {
            return updatedAccounts;
        }

        public Set<Account> getDeletedAccounts() {
            return deletedAccounts;
        }
    }
}
