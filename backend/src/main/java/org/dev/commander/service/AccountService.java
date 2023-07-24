package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.springframework.security.core.Authentication;

public interface AccountService {
    Account readAccount(Authentication authentication, Long id) throws IllegalArgumentException, NotFoundException;
    Account createAccount(Account account) throws IllegalArgumentException, ConflictException;
    Account updateAccount(Authentication authentication, Long id, Account account) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException, ConflictException;
    void deleteAccount(Authentication authentication, Long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException;
}
