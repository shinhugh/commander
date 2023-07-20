package org.dev.commander.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "GameMemberships")
@IdClass(GameMembership.Key.class)
public class GameMembership {
    @Column(name = "GameEntryId", nullable = false)
    @Id
    private Long gameEntryId;
    @Column(name = "AccountId", nullable = false)
    @Id
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
