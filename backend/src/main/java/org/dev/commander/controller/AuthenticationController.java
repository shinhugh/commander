package org.dev.commander.controller;

import org.dev.commander.model.Credentials;
import org.dev.commander.service.SessionService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationController {
    private final SessionService sessionService;

    public AuthenticationController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // TODO: Set X-Authorization cookie
    @PostMapping
    public String login(Authentication authentication, @RequestBody Credentials credentials) {
        return sessionService.login(authentication, credentials);
    }

    // TODO: Unset X-Authorization cookie
    @DeleteMapping
    public void logout(Authentication authentication, @RequestParam Map<String, String> parameters) {
        boolean all = false;
        if (parameters.containsKey("all")) {
            String value = parameters.get("all");
            if ("".equals(value) || "true".equals(value) || "1".equals(value)) {
                all = true;
            }
        }
        sessionService.logout(authentication, all);
    }
}
