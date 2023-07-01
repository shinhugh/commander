package org.dev.commander.service;

import org.dev.commander.model.Credentials;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {
    String login(Authentication authentication, Credentials credentials);
    void logout(Authentication authentication, boolean all);
}
