package org.dev.commander.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class WebSocketObjectDispatcher implements ObjectDispatcher, WebSocketRegistrar {
    private final Map<Long, Set<String>> accountIdToSessionTokenMap = new HashMap<>();
    private final Map<String, WebSocketSession> sessionTokenToConnectionMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendObject(long accountId, Object object) {
        Set<String> sessionTokens = accountIdToSessionTokenMap.get(accountId);
        if (sessionTokens == null) {
            return;
        }
        String message;
        try {
            message = objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException ex) {
            return; // TODO: Throw exception
        }
        for (Iterator<String> it = sessionTokens.iterator(); it.hasNext();) {
            String sessionToken = it.next();
            WebSocketSession connection = sessionTokenToConnectionMap.get(sessionToken);
            try {
                connection.sendMessage(new TextMessage(message));
            }
            catch (IOException ex) {
                try {
                    connection.close();
                }
                catch (IOException ignored) { }
                it.remove();
                sessionTokenToConnectionMap.remove(sessionToken);
            }
        }
        if (sessionTokens.isEmpty()) {
            accountIdToSessionTokenMap.remove(accountId);
        }
    }

    @Override
    public void registerConnection(WebSocketSession connection) {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void unregisterConnection(WebSocketSession connection) {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void closeConnectionForSession(String sessionToken) {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void closeConnectionsForAccount(long accountId) {
        // TODO: Implement
        throw new RuntimeException("Not implemented");
    }
}
