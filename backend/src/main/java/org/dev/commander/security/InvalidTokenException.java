package org.dev.commander.security;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
