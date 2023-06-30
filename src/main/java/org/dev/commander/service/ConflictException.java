package org.dev.commander.service;

public class ConflictException extends RuntimeException {
    public ConflictException() {
        super("Conflict");
    }

    public ConflictException(String message) {
        super(message);
    }
}
