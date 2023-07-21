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
    private Account friendAccount;

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

    public Account getFriendAccount() {
        return friendAccount;
    }

    public void setFriendAccount(Account friendAccount) {
        this.friendAccount = friendAccount;
    }

    public static class Key implements Serializable {
        private Long requestingAccountId;
        private Long respondingAccountId;

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
    }
}
