package org.dev.commander.service;

public class IllegalArgumentException extends RuntimeException {
    public IllegalArgumentException() {
        super("Illegal argument");
    }

    public IllegalArgumentException(String message) {
        super(message);
    }
}
