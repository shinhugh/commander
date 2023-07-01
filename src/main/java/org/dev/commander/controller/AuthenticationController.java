package org.dev.commander.controller;

import org.dev.commander.model.Credentials;
import org.dev.commander.service.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public String login(Authentication authentication, @RequestBody Credentials credentials) {
        return authenticationService.login(authentication, credentials);
    }

    @DeleteMapping
    public void logout(Authentication authentication, @RequestParam Map<String, String> parameters) {
        boolean all = false;
        if (parameters.containsKey("all")) {
            String value = parameters.get("all");
            if ("".equals(value) || "true".equals(value) || "1".equals(value)) {
                all = true;
            }
        }
        authenticationService.logout(authentication, all);
    }
}
