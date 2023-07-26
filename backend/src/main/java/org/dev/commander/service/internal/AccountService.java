package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;

import java.util.List;

public interface AccountService {
    List<Account> readAccounts(Long id, String loginName) throws IllegalArgumentException;
    Account createAccount(Account account) throws IllegalArgumentException, ConflictException;
    Account updateAccount(long id, Account account) throws IllegalArgumentException, NotFoundException, ConflictException;
    void deleteAccount(long id) throws IllegalArgumentException, NotFoundException;
    void registerAccountEventHandler(AccountEventHandler accountEventHandler);
}
