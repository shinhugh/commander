package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;

public interface AccountService {
    Account readAccountById(Authentication authentication, long id);
    Account createAccount(Authentication authentication, Account account);
    Account updateAccountById(Authentication authentication, long id, Account account);
    void deleteAccountById(Authentication authentication, long id);
}
