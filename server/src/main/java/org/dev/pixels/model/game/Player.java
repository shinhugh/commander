package org.dev.pixels.model.game;

import jakarta.persistence.*;

@Entity
@Table(name = "Players")
public class Player {
    @Column(name = "Id", nullable = false, unique = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "AccountId", nullable = false, unique = true)
    private long accountId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
