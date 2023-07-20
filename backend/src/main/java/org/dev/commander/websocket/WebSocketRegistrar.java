package org.dev.commander.websocket;

public interface WebSocketRegistrar {
    void closeConnectionForSession(String sessionToken);
    void closeConnectionsForAccount(long accountId);
}
