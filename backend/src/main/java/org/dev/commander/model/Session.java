package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Sessions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Session {
    @Column(name = "Token", nullable = false, unique = true)
    @Id
    private String token;
    @Column(name = "AccountId", nullable = false)
    private Long accountId;
    @Column(name = "Authorities", nullable = false)
    private Integer authorities;
    @Column(name = "CreationTime", nullable = false)
    private Long creationTime;
    @Column(name = "ExpirationTime", nullable = false)
    private Long expirationTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Integer authorities) {
        this.authorities = authorities;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
