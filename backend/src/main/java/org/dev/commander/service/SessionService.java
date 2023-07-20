package org.dev.commander.service;

import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.springframework.security.core.Authentication;

public interface SessionService {
    Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException;
    void logout(Authentication authentication, boolean all);
}
