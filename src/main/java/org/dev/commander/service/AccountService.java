package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;

public interface AccountService {
    Account readAccountById(Authentication authentication, long id) throws BadRequestException;
    Account createAccount(Authentication authentication, Account account) throws BadRequestException, ConflictException;
    Account updateAccountById(Authentication authentication, long id, Account account) throws UnauthorizedException, BadRequestException, ForbiddenException, ConflictException;
    void deleteAccountById(Authentication authentication, long id) throws UnauthorizedException, BadRequestException, ForbiddenException;
}
