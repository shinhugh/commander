package org.dev.commander.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @PostMapping
    public void login() {
        // TODO
    }

    @DeleteMapping
    public void logout(Authentication authentication) {
        // TODO
    }
}
