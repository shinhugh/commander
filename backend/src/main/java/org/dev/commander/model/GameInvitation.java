package org.dev.commander.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "GameInvitations")
@IdClass(GameInvitation.Key.class)
public class GameInvitation {
    @Column(name = "GameEntryId", nullable = false)
    @Id
    private Long gameEntryId;
    @Column(name = "AccountId", nullable = false)
    @Id
    private Long accountId;
    @Column(name = "CreationTime", nullable = false)
    private Long creationTime;
    @Column(name = "ExpirationTime", nullable = false)
    private Long expirationTime;

    public Long getGameEntryId() {
        return gameEntryId;
    }

    public void setGameEntryId(Long gameEntryId) {
        this.gameEntryId = gameEntryId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public static class Key implements Serializable {
        private Long gameEntryId;
        private Long accountId;

        public Long getGameEntryId() {
            return gameEntryId;
        }

        public void setGameEntryId(Long gameEntryId) {
            this.gameEntryId = gameEntryId;
        }

        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }
    }
}
