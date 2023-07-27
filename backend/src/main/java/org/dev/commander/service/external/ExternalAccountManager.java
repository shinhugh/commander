package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.dev.commander.service.internal.AccountService;
import org.dev.commander.service.internal.IdentificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ExternalAccountManager implements ExternalAccountService {
    private final AccountService accountService;
    private final IdentificationService identificationService;

    public ExternalAccountManager(AccountService accountService, IdentificationService identificationService) {
        this.accountService = accountService;
        this.identificationService = identificationService;
    }

    @Override
    public List<Account> readAccounts(Authentication authentication, Long id) throws IllegalArgumentException {
        if (id == null) {
            if (authentication == null) {
                throw new IllegalArgumentException();
            }
            id = ((Account) authentication.getPrincipal()).getId();
        }
        List<Account> accounts = accountService.readAccounts(id, null);
        for (Account account : accounts) {
            account.setPassword(null);
            if (!verifyClientIsOwnerOrAdmin(authentication, account)) {
                account.setLoginName(null);
                account.setAuthorities(null);
            }
        }
        return accounts;
    }

    @Override
    public Account createAccount(Authentication authentication, Account account) throws IllegalArgumentException, ConflictException {
        return accountService.createAccount(account);
    }

    @Override
    public Account updateAccount(Authentication authentication, Long id, Account account) throws IllegalArgumentException, NotFoundException, NotAuthenticatedException, NotAuthorizedException, ConflictException {
        if (id == null) {
            if (authentication == null) {
                throw new IllegalArgumentException();
            }
            id = ((Account) authentication.getPrincipal()).getId();
        }
        List<Account> existingAccounts = accountService.readAccounts(id, null);
        if (existingAccounts.isEmpty()) {
            throw new NotFoundException();
        }
        Account existingAccount = existingAccounts.get(0);
        if (authentication == null) {
            throw new NotAuthenticatedException();
        }
        if (!verifyClientIsOwnerOrAdmin(authentication, existingAccount)) {
            throw new NotAuthorizedException();
        }
        account = cloneAccount(account);
        if (!identificationService.verifyAtLeastOneAuthority(authentication, Set.of("ADMIN"))) {
            account.setAuthorities(existingAccount.getAuthorities());
        }
        if (account.getId() == null) {
            account.setId(existingAccount.getId());
        }
        if (account.getLoginName() == null) {
            account.setLoginName(existingAccount.getLoginName());
        }
        if (account.getPassword() == null) {
            account.setPassword(existingAccount.getPassword());
        }
        if (account.getAuthorities() == null) {
            account.setAuthorities(existingAccount.getAuthorities());
        }
        if (account.getPublicName() == null) {
            account.setPublicName(existingAccount.getPublicName());
        }
        account = accountService.updateAccount(id, account);
        account.setPassword(null);
        return account;
    }

    @Override
    public void deleteAccount(Authentication authentication, Long id) throws IllegalArgumentException, NotFoundException, NotAuthenticatedException, NotAuthorizedException {
        if (id == null) {
            if (authentication == null) {
                throw new IllegalArgumentException();
            }
            id = ((Account) authentication.getPrincipal()).getId();
        }
        List<Account> existingAccounts = accountService.readAccounts(id, null);
        if (existingAccounts.isEmpty()) {
            throw new NotFoundException();
        }
        Account existingAccount = existingAccounts.get(0);
        if (authentication == null) {
            throw new NotAuthenticatedException();
        }
        if (!verifyClientIsOwnerOrAdmin(authentication, existingAccount)) {
            throw new NotAuthorizedException();
        }
        accountService.deleteAccount(id);
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

    private boolean verifyClientIsOwnerOrAdmin(Authentication authentication, Account account) {
        if (authentication == null) {
            return false;
        }
        if (account.getLoginName().equals(authentication.getName())) {
            return true;
        }
        return identificationService.verifyAtLeastOneAuthority(authentication, Set.of("ADMIN"));
    }
}
