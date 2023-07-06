package org.dev.commander.service;

import org.dev.commander.model.Credentials;
import org.springframework.security.core.Authentication;

public interface SessionService {
    String login(Authentication authentication, Credentials credentials) throws BadRequestException, UnauthorizedException;
    void logout(Authentication authentication, boolean all);
}
