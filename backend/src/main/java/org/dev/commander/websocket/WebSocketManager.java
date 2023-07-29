package org.dev.commander.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.commander.model.Account;
import org.dev.commander.model.Session;
import org.dev.commander.security.TokenAuthenticationToken;
import org.dev.commander.service.internal.SessionEventHandler;
import org.dev.commander.service.internal.SessionService;
import org.dev.commander.websocket.exception.IllegalArgumentException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

// TODO: Make thread-safe
@Component
public class WebSocketManager extends TextWebSocketHandler implements OutgoingMessageSender, IncomingMessageReceiver, SessionEventHandler {
    private final Map<String, WebSocketSession> sessionTokenToConnectionMap = new HashMap<>();
    private final Map<Long, Set<String>> accountIdToSessionTokenMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<IncomingMessageHandler> incomingMessageHandlers = new HashSet<>();

    public WebSocketManager(SessionService sessionService) {
        sessionService.registerSessionEventHandler(this);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null || principal.getClass() != TokenAuthenticationToken.class) {
            try {
                session.close();
            }
            catch (IOException ignored) { }
            return;
        }
        TokenAuthenticationToken authenticationToken = (TokenAuthenticationToken) principal;
        String sessionToken = (String) authenticationToken.getCredentials();
        long accountId = ((Account) authenticationToken.getPrincipal()).getId();
        sessionTokenToConnectionMap.put(sessionToken, session);
        accountIdToSessionTokenMap.putIfAbsent(accountId, new HashSet<>());
        accountIdToSessionTokenMap.get(accountId).add(sessionToken);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Principal principal = session.getPrincipal();
        if (principal == null || principal.getClass() != TokenAuthenticationToken.class) {
            try {
                session.close();
            }
            catch (IOException ignored) { }
            return;
        }
        TokenAuthenticationToken authenticationToken = (TokenAuthenticationToken) principal;
        String sessionToken = (String) authenticationToken.getCredentials();
        long accountId = ((Account) authenticationToken.getPrincipal()).getId();
        accountIdToSessionTokenMap.get(accountId).remove(sessionToken);
        if (accountIdToSessionTokenMap.get(accountId).isEmpty()) {
            accountIdToSessionTokenMap.remove(accountId);
        }
        sessionTokenToConnectionMap.remove(sessionToken);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        // TODO: Parse message as JSON into IncomingMessage
        // TODO: Invoke all registered handlers
    }

    @Override
    public void sendObject(long accountId, Object object) throws IllegalArgumentException {
        Set<String> sessionTokens = accountIdToSessionTokenMap.get(accountId);
        if (sessionTokens == null) {
            return;
        }
        String message;
        try {
            message = objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException ex) {
            throw new IllegalArgumentException();
        }
        for (Iterator<String> it = sessionTokens.iterator(); it.hasNext();) {
            String sessionToken = it.next();
            WebSocketSession connection = sessionTokenToConnectionMap.get(sessionToken);
            try {
                connection.sendMessage(new TextMessage(message));
            }
            catch (IOException ex) {
                it.remove();
                sessionTokenToConnectionMap.remove(sessionToken);
                try {
                    connection.close();
                }
                catch (IOException ignored) { }
            }
        }
        if (sessionTokens.isEmpty()) {
            accountIdToSessionTokenMap.remove(accountId);
        }
    }

    @Override
    public void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler) {
        incomingMessageHandlers.add(incomingMessageHandler);
    }

    @Override
    public void handleLogin(Session newSession) { }

    @Override
    public void handleLogout(Session deletedSession) {
        String sessionToken = deletedSession.getToken();
        long accountId = deletedSession.getAccountId();
        Set<String> accountSessionTokens = accountIdToSessionTokenMap.get(accountId);
        if (accountSessionTokens != null) {
            accountSessionTokens.remove(sessionToken);
            if (accountSessionTokens.isEmpty()) {
                accountIdToSessionTokenMap.remove(accountId);
            }
        }
        WebSocketSession connection = sessionTokenToConnectionMap.remove(sessionToken);
        if (connection != null) {
            try {
                connection.close();
            }
            catch (IOException ignored) { }
        }
    }
}
