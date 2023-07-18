package org.dev.commander.security;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

public class TokenAuthenticationToken implements Authentication {
    private final String token;
    private final Account account;
    private final Set<GrantedAuthority> authorities;

    public TokenAuthenticationToken(String token) {
        this.token = token;
        account = null;
        authorities = null;
    }

    public TokenAuthenticationToken(String token, Account account, Set<GrantedAuthority> authorities) {
        this.token = token;
        this.account = account;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return account;
    }

    @Override
    public boolean isAuthenticated() {
        return account != null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }

    @Override
    public String getName() {
        return account == null ? null : account.getLoginName();
    }
}
