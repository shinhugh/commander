package org.dev.commander.websocket;

import org.dev.commander.model.Session;
import org.dev.commander.service.internal.SessionService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class SecurityHandshakeInterceptor implements HandshakeInterceptor {
    private final SessionService sessionService;

    public SecurityHandshakeInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // TODO: Send status code 401 if this method returns false (currently sends 200)
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String token = (String) authentication.getCredentials();
        return identifySession(token) != null;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) { }

    private Session identifySession(String token) {
        List<Session> sessions = sessionService.readSessions(token, null);
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.get(0);
    }
}
