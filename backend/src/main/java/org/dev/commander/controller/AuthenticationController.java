package org.dev.commander.controller;

import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.SessionService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Order(-1)
public class AuthenticationController {
    private final SessionService sessionService;

    public AuthenticationController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Session> login(Authentication authentication, @RequestBody Credentials credentials) {
        Session session = sessionService.login(authentication, credentials);
        long maxAge = (session.getExpirationTime() - session.getCreationTime()) / 1000;
        String xAuthorizationCookieHeaderValue = "X-Authorization=" + session.getToken() + "; Max-Age=" + maxAge;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", xAuthorizationCookieHeaderValue);
        session.setAccountId(0);
        session.setAuthorities(0);
        session.setCreationTime(0);
        return new ResponseEntity<>(session, headers, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> logout(Authentication authentication, @RequestParam Map<String, String> parameters) {
        boolean all = false;
        if (parameters.containsKey("all")) {
            String value = parameters.get("all");
            if ("".equals(value) || "true".equals(value) || "1".equals(value)) {
                all = true;
            }
        }
        sessionService.logout(authentication, all);
        String xAuthorizationCookieHeaderValue = "X-Authorization=; Max-Age=0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", xAuthorizationCookieHeaderValue);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
    }
}
