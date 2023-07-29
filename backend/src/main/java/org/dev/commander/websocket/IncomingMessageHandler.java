package org.dev.commander.websocket;

import org.dev.commander.model.IncomingMessage;

public interface IncomingMessageHandler {
    void handleIncomingMessage(IncomingMessage<?> message);
}
