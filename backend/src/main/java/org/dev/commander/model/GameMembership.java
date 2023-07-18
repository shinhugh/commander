package org.dev.commander.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "GameMemberships")
@IdClass(GameMembership.Key.class)
public class GameMembership {
    @Column(name = "GameEntryId", nullable = false)
    @Id
    private long gameEntryId;
    @Column(name = "AccountId", nullable = false)
    @Id
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
