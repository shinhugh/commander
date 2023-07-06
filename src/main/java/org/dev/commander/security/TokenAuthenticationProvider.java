package org.dev.commander.security;

import org.dev.commander.model.Account;
import org.dev.commander.model.Session;
import org.dev.commander.service.AuthenticationService;
import org.dev.commander.service.NotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {
    private static final List<GrantedAuthority> AUTHORITIES_IN_ORDER = Arrays.asList(
            new SimpleGrantedAuthority("USER"),
            new SimpleGrantedAuthority("ADMIN")
    );
    private final AuthenticationService authenticationService;

    TokenAuthenticationProvider(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null || authentication.getClass() != TokenAuthenticationToken.class) {
            return null;
        }
        String token = (String) authentication.getCredentials();
        Session session;
        try {
            session = authenticationService.getSession(token);
        }
        catch (NotFoundException ex) {
            throw new InvalidTokenException();
        }
        Account account = authenticationService.getSessionOwner(session);
        Set<GrantedAuthority> authorities = translateAuthoritiesFlagToSet(session.getAuthorities());
        return new TokenAuthenticationToken(token, account, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == TokenAuthenticationToken.class;
    }

    private Set<GrantedAuthority> translateAuthoritiesFlagToSet(int flag) {
        Set<GrantedAuthority> set = new HashSet<>();
        int order = 0;
        while (flag > 0 && order < AUTHORITIES_IN_ORDER.size()) {
            if (flag % 2 > 0) {
                set.add(AUTHORITIES_IN_ORDER.get(order));
            }
            flag /= 2;
            order++;
        }
        return set;
    }
}
