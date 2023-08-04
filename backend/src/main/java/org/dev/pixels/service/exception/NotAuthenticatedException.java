package org.dev.pixels.service.exception;

public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
        super("Not authenticated");
    }

    public NotAuthenticatedException(String message) {
        super(message);
    }
}
