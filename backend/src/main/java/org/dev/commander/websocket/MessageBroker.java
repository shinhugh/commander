package org.dev.commander.websocket;

import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.websocket.exception.IllegalArgumentException;

public interface MessageBroker {
    void registerConnectionEventHandler(ConnectionEventHandler connectionEventHandler);
    void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler);
    void sendMessageByAccountId(long accountId, OutgoingMessage<?> object) throws IllegalArgumentException;
    void sendMessageBySessionToken(String sessionToken, OutgoingMessage<?> object) throws IllegalArgumentException;
}
