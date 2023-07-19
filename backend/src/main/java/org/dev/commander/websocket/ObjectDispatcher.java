package org.dev.commander.websocket;

import org.dev.commander.websocket.exception.IllegalArgumentException;

public interface ObjectDispatcher {
    void sendObject(long accountId, Object object) throws IllegalArgumentException;
}
