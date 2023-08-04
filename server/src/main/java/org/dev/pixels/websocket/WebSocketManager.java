package org.dev.pixels.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.pixels.model.IncomingMessage;
import org.dev.pixels.model.OutgoingMessage;
import org.dev.pixels.model.Session;
import org.dev.pixels.security.TokenAuthenticationToken;
import org.dev.pixels.service.internal.IdentificationService;
import org.dev.pixels.service.internal.SessionEventHandler;
import org.dev.pixels.service.internal.SessionService;
import org.dev.pixels.websocket.exception.IllegalArgumentException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class WebSocketManager extends TextWebSocketHandler implements MessageBroker, SessionEventHandler {
    private final IdentificationService identificationService;
    private final Map<String, WebSocketSession> sessionTokenToConnectionMap = new HashMap<>();
    private final Lock sessionTokenToConnectionMapReadLock;
    private final Lock sessionTokenToConnectionMapWriteLock;
    private final Map<Long, Set<String>> accountIdToSessionTokenMap = new HashMap<>();
    private final Lock accountIdToSessionTokenMapReadLock;
    private final Lock accountIdToSessionTokenMapWriteLock;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<ConnectionEventHandler> connectionEventHandlers = new HashSet<>();
    private final Lock connectionEventHandlersReadLock;
    private final Lock connectionEventHandlersWriteLock;
    private final Set<IncomingMessageHandler> incomingMessageHandlers = new HashSet<>();
    private final Lock incomingMessageHandlersReadLock;
    private final Lock incomingMessageHandlersWriteLock;

    public WebSocketManager(IdentificationService identificationService, SessionService sessionService) {
        this.identificationService = identificationService;
        ReadWriteLock sessionTokenToConnectionMapReadWriteLock = new ReentrantReadWriteLock();
        sessionTokenToConnectionMapReadLock = sessionTokenToConnectionMapReadWriteLock.readLock();
        sessionTokenToConnectionMapWriteLock = sessionTokenToConnectionMapReadWriteLock.writeLock();
        ReadWriteLock accountIdToSessionTokenMapReadWriteLock = new ReentrantReadWriteLock();
        accountIdToSessionTokenMapReadLock = accountIdToSessionTokenMapReadWriteLock.readLock();
        accountIdToSessionTokenMapWriteLock = accountIdToSessionTokenMapReadWriteLock.writeLock();
        ReadWriteLock connectionEventHandlersReadWriteLock = new ReentrantReadWriteLock();
        connectionEventHandlersReadLock = connectionEventHandlersReadWriteLock.readLock();
        connectionEventHandlersWriteLock = connectionEventHandlersReadWriteLock.writeLock();
        ReadWriteLock incomingMessageHandlersReadWriteLock = new ReentrantReadWriteLock();
        incomingMessageHandlersReadLock = incomingMessageHandlersReadWriteLock.readLock();
        incomingMessageHandlersWriteLock = incomingMessageHandlersReadWriteLock.writeLock();
        sessionService.registerSessionEventHandler(this);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        TokenAuthenticationToken authentication = (TokenAuthenticationToken) session.getPrincipal();
        if (authentication == null) {
            try {
                session.close();
            }
            catch (IOException ignored) { }
            return;
        }
        String sessionToken = (String) authentication.getCredentials();
        long accountId = identificationService.identifyAccount(authentication).getId();
        sessionTokenToConnectionMapWriteLock.lock();
        try {
            sessionTokenToConnectionMap.put(sessionToken, session);
        }
        finally {
            sessionTokenToConnectionMapWriteLock.unlock();
        }
        accountIdToSessionTokenMapWriteLock.lock();
        try {
            accountIdToSessionTokenMap.putIfAbsent(accountId, new HashSet<>());
            accountIdToSessionTokenMap.get(accountId).add(sessionToken);
        }
        finally {
            accountIdToSessionTokenMapWriteLock.unlock();
        }
        Set<ConnectionEventHandler> handlers;
        connectionEventHandlersReadLock.lock();
        try {
            handlers = new HashSet<>(connectionEventHandlers);
        }
        finally {
            connectionEventHandlersReadLock.unlock();
        }
        for (ConnectionEventHandler handler : handlers) {
            handler.handleEstablishedConnection(authentication);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        TokenAuthenticationToken authentication = (TokenAuthenticationToken) session.getPrincipal();
        if (authentication == null) {
            return;
        }
        String sessionToken = (String) authentication.getCredentials();
        long accountId = identificationService.identifyAccount(authentication).getId();
        accountIdToSessionTokenMapWriteLock.lock();
        try {
            accountIdToSessionTokenMap.get(accountId).remove(sessionToken);
            if (accountIdToSessionTokenMap.get(accountId).isEmpty()) {
                accountIdToSessionTokenMap.remove(accountId);
            }
        }
        finally {
            accountIdToSessionTokenMapWriteLock.unlock();
        }
        sessionTokenToConnectionMapWriteLock.lock();
        try {
            sessionTokenToConnectionMap.remove(sessionToken);
        }
        finally {
            sessionTokenToConnectionMapWriteLock.unlock();
        }
        Set<ConnectionEventHandler> handlers;
        connectionEventHandlersReadLock.lock();
        try {
            handlers = new HashSet<>(connectionEventHandlers);
        }
        finally {
            connectionEventHandlersReadLock.unlock();
        }
        for (ConnectionEventHandler handler : handlers) {
            handler.handleClosedConnection(authentication);
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        TokenAuthenticationToken authentication = (TokenAuthenticationToken) session.getPrincipal();
        if (authentication == null) {
            return;
        }
        IncomingMessage incomingMessage;
        try {
            incomingMessage = objectMapper.readValue(message.getPayload(), IncomingMessage.class);
        }
        catch (JsonProcessingException ex) {
            return;
        }
        Set<IncomingMessageHandler> handlers;
        incomingMessageHandlersReadLock.lock();
        try {
            handlers = new HashSet<>(incomingMessageHandlers);
        }
        finally {
            incomingMessageHandlersReadLock.unlock();
        }
        for (IncomingMessageHandler handler : handlers) {
            handler.handleIncomingMessage(authentication, incomingMessage);
        }
    }

    @Override
    public void registerConnectionEventHandler(ConnectionEventHandler connectionEventHandler) {
        connectionEventHandlersWriteLock.lock();
        try {
            connectionEventHandlers.add(connectionEventHandler);
        }
        finally {
            connectionEventHandlersWriteLock.unlock();
        }
    }

    @Override
    public void registerIncomingMessageHandler(IncomingMessageHandler incomingMessageHandler) {
        incomingMessageHandlersWriteLock.lock();
        try {
            incomingMessageHandlers.add(incomingMessageHandler);
        }
        finally {
            incomingMessageHandlersWriteLock.unlock();
        }
    }

    @Override
    public void sendMessageByAccountId(long accountId, OutgoingMessage<?> object) throws IllegalArgumentException {
        if (accountId <= 0) {
            throw new IllegalArgumentException();
        }
        Set<String> sessionTokens;
        accountIdToSessionTokenMapReadLock.lock();
        try {
            sessionTokens = accountIdToSessionTokenMap.get(accountId);
            if (sessionTokens != null) {
                sessionTokens = new HashSet<>(sessionTokens);
            }
        }
        finally {
            accountIdToSessionTokenMapReadLock.unlock();
        }
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
        for (String sessionToken : sessionTokens) {
            WebSocketSession connection;
            sessionTokenToConnectionMapReadLock.lock();
            try {
                connection = sessionTokenToConnectionMap.get(sessionToken);
            }
            finally {
                sessionTokenToConnectionMapReadLock.unlock();
            }
            try {
                connection.sendMessage(new TextMessage(message));
            }
            catch (IOException ex) {
                try {
                    connection.close();
                }
                catch (IOException ignored) { }
            }
        }
    }

    @Override
    public void sendMessageBySessionToken(String sessionToken, OutgoingMessage<?> object) throws IllegalArgumentException {
        if (sessionToken == null || sessionToken.length() == 0) {
            throw new IllegalArgumentException();
        }
        WebSocketSession connection;
        sessionTokenToConnectionMapReadLock.lock();
        try {
            connection = sessionTokenToConnectionMap.get(sessionToken);
        }
        finally {
            sessionTokenToConnectionMapReadLock.unlock();
        }
        if (connection == null) {
            return;
        }
        String message;
        try {
            message = objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException ex) {
            throw new IllegalArgumentException();
        }
        try {
            connection.sendMessage(new TextMessage(message));
        }
        catch (IOException ex) {
            try {
                connection.close();
            }
            catch (IOException ignored) { }
        }
    }

    @Override
    public void handleLogin(Session newSession) { }

    @Override
    public void handleLogout(Session deletedSession) {
        WebSocketSession connection;
        sessionTokenToConnectionMapReadLock.lock();
        try {
            connection = sessionTokenToConnectionMap.get(deletedSession.getToken());
        }
        finally {
            sessionTokenToConnectionMapReadLock.unlock();
        }
        if (connection != null) {
            try {
                connection.close();
            }
            catch (IOException ignored) { }
        }
    }
}
