package org.dev.commander.model.game;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Not found");
    }
}
