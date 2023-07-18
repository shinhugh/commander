package org.dev.commander.service.exception;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException() {
        super("Not authorized");
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
