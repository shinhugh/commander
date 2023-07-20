package org.dev.commander.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private final AuthenticationManager authenticationManager;

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("authorization");
        Cookie xAuthorizationCookie = null;
        if (request.getCookies() != null) {
            xAuthorizationCookie = Arrays.stream(request.getCookies()).filter(c -> "X-Authorization".equals(c.getName())).findFirst().orElse(null);
        }
        String token;
        if (authorizationHeader != null && authorizationHeader.indexOf("Bearer") == 0 && authorizationHeader.length() >= 8) {
            token = authorizationHeader.substring(7);
        }
        else if (xAuthorizationCookie != null && xAuthorizationCookie.getValue() != null) {
            token = xAuthorizationCookie.getValue();
        }
        else {
            filterChain.doFilter(request, response);
            return;
        }
        TokenAuthenticationToken attempt = new TokenAuthenticationToken(token);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(attempt);
        }
        catch (AuthenticationException ex) {
            filterChain.doFilter(request, response);
            return;
        }
        // TODO: Is this the right/standard way to authenticate the client?
        SecurityContext securityContext = securityContextHolderStrategy.createEmptyContext();
        securityContext.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
        filterChain.doFilter(request, response);
    }
}
