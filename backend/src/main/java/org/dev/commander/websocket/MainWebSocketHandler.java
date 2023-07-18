package org.dev.commander.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class MainWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // TODO: Register session; map account to session
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // TODO: Unregister session
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // TODO: Close connection if not authenticated
        // TODO: Close connection if existing authentication's token does not exist
        // TODO: Parse message as JSON
    }
}
