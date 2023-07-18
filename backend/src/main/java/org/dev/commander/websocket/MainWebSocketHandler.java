package org.dev.commander.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class MainWebSocketHandler extends TextWebSocketHandler { // TODO: Consider merging this with WebSocketObjectDispatcher
    private final WebSocketRegistrar webSocketRegistrar;

    public MainWebSocketHandler(WebSocketRegistrar webSocketRegistrar) {
        this.webSocketRegistrar = webSocketRegistrar;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketRegistrar.registerConnection(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketRegistrar.unregisterConnection(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // TODO: Parse message as JSON
        // TODO: Delegate parsed message to appropriate service
    }
}
