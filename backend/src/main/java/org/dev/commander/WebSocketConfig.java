package org.dev.commander;

import org.dev.commander.websocket.SecurityHandshakeInterceptor;
import org.dev.commander.websocket.WebSocketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@ComponentScan("org.dev.commander.websocket")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final String DEV_ORIGIN = "http://localhost";

    @Autowired
    private WebSocketManager webSocketManager;
    @Autowired
    private SecurityHandshakeInterceptor securityHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        ((ServletWebSocketHandlerRegistry) registry).setOrder(-1);
        WebSocketHandlerRegistration registration = registry.addHandler(webSocketManager, "/ws");
        String origin = System.getenv("ORIGIN");
        if (origin == null) {
            registration.setAllowedOrigins(DEV_ORIGIN);
        } else {
            registration.setAllowedOrigins(DEV_ORIGIN, origin);
        }
        registration.addInterceptors(securityHandshakeInterceptor);
    }
}
