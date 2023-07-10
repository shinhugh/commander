package org.dev.commander;

import org.dev.commander.websocket.TestWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.ServletWebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@ComponentScan("org.dev.commander.websocket")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    TestWebSocketHandler testWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (registry.getClass() == ServletWebSocketHandlerRegistry.class) {
            ((ServletWebSocketHandlerRegistry) registry).setOrder(Ordered.HIGHEST_PRECEDENCE);
        }
        registry.addHandler(testWebSocketHandler, "/api/test");
    }
}
