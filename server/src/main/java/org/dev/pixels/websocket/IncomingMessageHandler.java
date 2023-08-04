package org.dev.pixels.websocket;

import org.dev.pixels.model.IncomingMessage;
import org.springframework.security.core.Authentication;

public interface IncomingMessageHandler {
    void handleIncomingMessage(Authentication authentication, IncomingMessage message);
}
