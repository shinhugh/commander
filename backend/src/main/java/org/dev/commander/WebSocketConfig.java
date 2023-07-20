package org.dev.commander;

import org.dev.commander.websocket.SecurityHandshakeInterceptor;
import org.dev.commander.websocket.WebSocketObjectDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.ServletWebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@ComponentScan("org.dev.commander.websocket")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private WebSocketObjectDispatcher webSocketObjectDispatcher;
    @Autowired
    private SecurityHandshakeInterceptor securityHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (registry.getClass() == ServletWebSocketHandlerRegistry.class) {
            ((ServletWebSocketHandlerRegistry) registry).setOrder(-1);
        }
        registry
                .addHandler(webSocketObjectDispatcher, "/api/ws")
                .addInterceptors(securityHandshakeInterceptor);
    }
}
