package org.dev.pixels.service.external;

import org.dev.pixels.model.Credentials;
import org.dev.pixels.model.Session;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotAuthenticatedException;
import org.springframework.security.core.Authentication;

public interface ExternalAuthenticationService {
    Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException;
    void logout(Authentication authentication, boolean all);
}
