package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {
    Session identifySession(String token) throws NotFoundException;
    Account identifyAccount(String token) throws NotFoundException;
    Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException;
    void logout(Authentication authentication, Boolean all);
}
