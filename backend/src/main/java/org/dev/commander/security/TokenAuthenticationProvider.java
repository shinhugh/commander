package org.dev.commander.security;

import org.dev.commander.model.Account;
import org.dev.commander.model.Session;
import org.dev.commander.security.exception.InvalidTokenException;
import org.dev.commander.service.internal.AccountService;
import org.dev.commander.service.internal.SessionService;
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
    private final SessionService sessionService;
    private final AccountService accountService;

    TokenAuthenticationProvider(SessionService sessionService, AccountService accountService) {
        this.sessionService = sessionService;
        this.accountService = accountService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            return null;
        }
        String token = (String) authentication.getCredentials();
        Session session = identifySession(token);
        if (session == null) {
            throw new InvalidTokenException();
        }
        Account account = identifyAccount(token);
        if (account == null) {
            throw new InvalidTokenException();
        }
        Set<GrantedAuthority> authorities = translateAuthoritiesFlagToSet(session.getAuthorities());
        return new TokenAuthenticationToken(token, account, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == TokenAuthenticationToken.class;
    }

    private Session identifySession(String token) {
        List<Session> sessions = sessionService.readSessions(token, null);
        if (sessions.isEmpty()) {
            return null;
        }
        return sessions.get(0);
    }

    private Account identifyAccount(String token) {
        Session session = identifySession(token);
        if (session == null) {
            return null;
        }
        List<Account> accounts = accountService.readAccounts(session.getAccountId(), null);
        if (accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0);
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
