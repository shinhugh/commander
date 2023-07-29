package org.dev.commander.websocket;

import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.websocket.exception.IllegalArgumentException;

public interface MessageBroker {
    void sendMessageByAccountId(long accountId, OutgoingMessage<?> object) throws IllegalArgumentException;
    void sendMessageBySessionToken(String sessionToken, OutgoingMessage<?> object) throws IllegalArgumentException;
    void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler);
}
