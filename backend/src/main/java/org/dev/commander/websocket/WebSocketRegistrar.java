package org.dev.commander.websocket;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketRegistrar {
    void registerConnection(WebSocketSession connection);
    void unregisterConnection(WebSocketSession connection);
    void closeConnectionForSession(String sessionToken);
    void closeConnectionsForAccount(long accountId);
}
