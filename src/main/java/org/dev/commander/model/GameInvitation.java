package org.dev.commander.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "GameInvitations")
@IdClass(GameInvitation.Key.class)
public class GameInvitation {
    @Column(name = "GameEntryId", nullable = false)
    @Id
    private long gameEntryId;
    @Column(name = "AccountId", nullable = false)
    @Id
    private long accountId;
    @Column(name = "CreationTime", nullable = false)
    private long creationTime;
    @Column(name = "ExpirationTime", nullable = false)
    private long expirationTime;

    public long getGameEntryId() {
        return gameEntryId;
    }

    public void setGameEntryId(long gameEntryId) {
        this.gameEntryId = gameEntryId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
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

    public static class Key implements Serializable {
        private long gameEntryId;
        private long accountId;

        public long getGameEntryId() {
            return gameEntryId;
        }

        public void setGameEntryId(long gameEntryId) {
            this.gameEntryId = gameEntryId;
        }

        public long getAccountId() {
            return accountId;
        }

        public void setAccountId(long accountId) {
            this.accountId = accountId;
        }
    }
}
