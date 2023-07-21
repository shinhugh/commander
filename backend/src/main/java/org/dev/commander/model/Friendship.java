package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "Friendships")
@IdClass(Friendship.Key.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Friendship {
    @Column(name = "RequestingAccountId", nullable = false)
    @Id
    private Long requestingAccountId;
    @Column(name = "RespondingAccountId", nullable = false)
    @Id
    private Long respondingAccountId;
    @Column(name = "Accepted", nullable = false)
    private Boolean accepted;
    @Column(name = "CreationTime", nullable = false)
    private Long creationTime;
    @Transient
    private Account requestingAccount;
    @Transient
    private Account respondingAccount;

    public Long getRequestingAccountId() {
        return requestingAccountId;
    }

    public void setRequestingAccountId(Long requestingAccountId) {
        this.requestingAccountId = requestingAccountId;
    }

    public Long getRespondingAccountId() {
        return respondingAccountId;
    }

    public void setRespondingAccountId(Long respondingAccountId) {
        this.respondingAccountId = respondingAccountId;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Account getRequestingAccount() {
        return requestingAccount;
    }

    public void setRequestingAccount(Account requestingAccount) {
        this.requestingAccount = requestingAccount;
    }

    public Account getRespondingAccount() {
        return respondingAccount;
    }

    public void setRespondingAccount(Account respondingAccount) {
        this.respondingAccount = respondingAccount;
    }

    public static class Key implements Serializable {
        private Long accountAId;
        private Long accountBId;

        public Long getAccountAId() {
            return accountAId;
        }

        public void setAccountAId(Long accountAId) {
            this.accountAId = accountAId;
        }

        public Long getAccountBId() {
            return accountBId;
        }

        public void setAccountBId(Long accountBId) {
            this.accountBId = accountBId;
        }
    }
}
