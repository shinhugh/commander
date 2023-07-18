package org.dev.commander.websocket;

import org.dev.commander.service.AuthenticationService;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class SecurityHandshakeInterceptor implements HandshakeInterceptor {
    private final AuthenticationService authenticationService;

    public SecurityHandshakeInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Object credentials = authentication.getCredentials();
        if (credentials == null || credentials.getClass() != String.class) {
            return false;
        }
        try {
            authenticationService.getSession((String) credentials);
        }
        catch (NotFoundException ex) {
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) { }
}
