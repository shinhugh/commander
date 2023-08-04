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
        String originVar = System.getenv("ORIGINS");
        if (originVar == null) {
            registration.setAllowedOrigins(DEV_ORIGIN);
        } else {
            String[] origins = originVar.split(",");
            String[] originsWithDev = new String[origins.length + 1];
            System.arraycopy(origins, 0, originsWithDev, 0, origins.length);
            originsWithDev[originsWithDev.length - 1] = DEV_ORIGIN;
            registration.setAllowedOrigins(originsWithDev);
        }
        registration.addInterceptors(securityHandshakeInterceptor);
    }
}
