package org.dev.commander.model.game;

public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException() {
        super("Invalid move");
    }
}
