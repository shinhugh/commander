package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ExternalAccountService {
    List<Account> readAccounts(Authentication authentication, Long id) throws IllegalArgumentException;
    Account createAccount(Authentication authentication, Account account) throws IllegalArgumentException, ConflictException;
    Account updateAccount(Authentication authentication, Long id, Account account) throws IllegalArgumentException, NotFoundException, NotAuthenticatedException, NotAuthorizedException, ConflictException;
    void deleteAccount(Authentication authentication, Long id) throws IllegalArgumentException, NotFoundException, NotAuthenticatedException, NotAuthorizedException;
}
