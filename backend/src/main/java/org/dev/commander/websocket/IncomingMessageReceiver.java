package org.dev.commander.websocket;

public interface IncomingMessageReceiver {
    void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler);
}
