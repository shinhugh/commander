package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.springframework.security.core.Authentication;

public interface AccountService {
    Account readAccountById(Authentication authentication, long id) throws IllegalArgumentException, NotFoundException;
    Account createAccount(Authentication authentication, Account account) throws IllegalArgumentException, ConflictException;
    Account updateAccountById(Authentication authentication, long id, Account account) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException, ConflictException;
    void deleteAccountById(Authentication authentication, long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException;
}
