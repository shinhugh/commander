package org.dev.commander.websocket;

import org.springframework.security.core.Authentication;

public interface ConnectionEventHandler {
    void handleEstablishedConnection(Authentication authentication);
    void handleClosedConnection(Authentication authentication);
}
