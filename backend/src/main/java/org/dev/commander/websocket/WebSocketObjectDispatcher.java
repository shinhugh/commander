package org.dev.commander.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.commander.model.Account;
import org.dev.commander.security.TokenAuthenticationToken;
import org.dev.commander.websocket.exception.IllegalArgumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

// TODO: Make thread-safe
@Component
public class WebSocketObjectDispatcher implements ObjectDispatcher, WebSocketRegistrar {
    private final Map<String, WebSocketSession> sessionTokenToConnectionMap = new HashMap<>();
    private final Map<Long, Set<String>> accountIdToSessionTokenMap = new HashMap<>();
    private final Map<String, Long> sessionTokenToAccountIdMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                sessionTokenToAccountIdMap.remove(sessionToken);
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
    public void registerConnection(WebSocketSession connection) {
        Principal principal = connection.getPrincipal();
        if (principal == null || principal.getClass() != TokenAuthenticationToken.class) {
            try {
                connection.close();
            }
            catch (IOException ignored) { }
            return;
        }
        TokenAuthenticationToken authenticationToken = (TokenAuthenticationToken) principal;
        String sessionToken = (String) authenticationToken.getCredentials();
        long accountId = ((Account) authenticationToken.getPrincipal()).getId();
        sessionTokenToConnectionMap.put(sessionToken, connection);
        accountIdToSessionTokenMap.putIfAbsent(accountId, new HashSet<>());
        accountIdToSessionTokenMap.get(accountId).add(sessionToken);
        sessionTokenToAccountIdMap.put(sessionToken, accountId);
    }

    @Override
    public void unregisterConnection(WebSocketSession connection) {
        Principal principal = connection.getPrincipal();
        if (principal == null || principal.getClass() != TokenAuthenticationToken.class) {
            try {
                connection.close();
            }
            catch (IOException ignored) { }
            return;
        }
        TokenAuthenticationToken authenticationToken = (TokenAuthenticationToken) principal;
        String sessionToken = (String) authenticationToken.getCredentials();
        long accountId = ((Account) authenticationToken.getPrincipal()).getId();
        sessionTokenToAccountIdMap.remove(sessionToken);
        accountIdToSessionTokenMap.get(accountId).remove(sessionToken);
        if (accountIdToSessionTokenMap.get(accountId).isEmpty()) {
            accountIdToSessionTokenMap.remove(accountId);
        }
        sessionTokenToConnectionMap.remove(sessionToken);
    }

    @Override
    public void closeConnectionForSession(String sessionToken) {
        Long accountId = sessionTokenToAccountIdMap.get(sessionToken);
        if (accountId == null) {
            return;
        }
        sessionTokenToAccountIdMap.remove(sessionToken);
        accountIdToSessionTokenMap.get(accountId).remove(sessionToken);
        WebSocketSession connection = sessionTokenToConnectionMap.remove(sessionToken);
        try {
            connection.close();
        }
        catch (IOException ignored) { }
    }

    @Override
    public void closeConnectionsForAccount(long accountId) {
        Set<String> sessionTokens = accountIdToSessionTokenMap.get(accountId);
        if (sessionTokens == null) {
            return;
        }
        for (String sessionToken : sessionTokens) {
            sessionTokenToAccountIdMap.remove(sessionToken);
            WebSocketSession connection = sessionTokenToConnectionMap.remove(sessionToken);
            try {
                connection.close();
            }
            catch (IOException ignored) { }
        }
        accountIdToSessionTokenMap.remove(accountId);
    }
}
