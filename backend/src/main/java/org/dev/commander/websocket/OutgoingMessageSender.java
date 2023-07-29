package org.dev.commander.websocket;

import org.dev.commander.websocket.exception.IllegalArgumentException;

public interface OutgoingMessageSender {
    void sendObjectByAccountId(long accountId, Object object) throws IllegalArgumentException;
    void sendObjectBySessionToken(String sessionToken, Object object) throws IllegalArgumentException;
}
