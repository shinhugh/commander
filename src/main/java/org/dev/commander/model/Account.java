package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

@Entity
@Table(name = "Accounts")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Account {
    @Column(name = "Id", nullable = false, unique = true)
    @Id
    @GeneratedValue
    private long id;
    @Column(name = "LoginName", nullable = false, unique = true)
    private String loginName;
    @Column(name = "Password", nullable = false)
    private String password;
    @Column(name = "Authorities", nullable = false)
    private int authorities;
    @Column(name = "PublicName", nullable = false)
    private String publicName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAuthorities() {
        return authorities;
    }

    public void setAuthorities(int authorities) {
        this.authorities = authorities;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }
}
