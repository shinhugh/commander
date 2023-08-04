package org.dev.pixels.websocket;

import org.dev.pixels.model.OutgoingMessage;
import org.dev.pixels.websocket.exception.IllegalArgumentException;

public interface MessageBroker {
    void registerConnectionEventHandler(ConnectionEventHandler connectionEventHandler);
    void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler);
    void sendMessageByAccountId(long accountId, OutgoingMessage<?> object) throws IllegalArgumentException;
    void sendMessageBySessionToken(String sessionToken, OutgoingMessage<?> object) throws IllegalArgumentException;
}
