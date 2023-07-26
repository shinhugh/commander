package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.dev.commander.service.internal.IdentificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AccountManager implements AccountService {
    private final org.dev.commander.service.internal.AccountService accountService;
    private final IdentificationService identificationService;

    public AccountManager(org.dev.commander.service.internal.AccountService accountService, IdentificationService identificationService) {
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
        // TODO: Only admin should be able to specify authorities
        // TODO: Interpret null fields as "don't change"
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
