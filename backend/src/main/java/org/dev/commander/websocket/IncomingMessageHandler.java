package org.dev.commander.websocket;

import org.dev.commander.model.IncomingMessage;
import org.springframework.security.core.Authentication;

public interface IncomingMessageHandler {
    void handleIncomingMessage(Authentication authentication, IncomingMessage<?> message);
}
