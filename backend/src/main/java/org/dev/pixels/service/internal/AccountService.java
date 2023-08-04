package org.dev.pixels.service.internal;

import org.dev.pixels.model.Account;
import org.dev.pixels.service.exception.ConflictException;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotFoundException;

import java.util.List;

public interface AccountService {
    List<Account> readAccounts(Long id, String loginName) throws IllegalArgumentException;
    Account createAccount(Account account) throws IllegalArgumentException, ConflictException;
    Account updateAccount(long id, Account account) throws IllegalArgumentException, NotFoundException, ConflictException;
    void deleteAccount(long id) throws IllegalArgumentException, NotFoundException;
    void registerAccountEventHandler(AccountEventHandler accountEventHandler);
}
