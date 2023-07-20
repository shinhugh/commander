package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Sessions")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Session {
    @Column(name = "Token", nullable = false, unique = true)
    @Id
    private String token;
    @Column(name = "AccountId", nullable = false)
    private long accountId;
    @Column(name = "Authorities", nullable = false)
    private int authorities;
    @Column(name = "CreationTime", nullable = false)
    private long creationTime;
    @Column(name = "ExpirationTime", nullable = false)
    private long expirationTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getAuthorities() {
        return authorities;
    }

    public void setAuthorities(int authorities) {
        this.authorities = authorities;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
