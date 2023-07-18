package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "GameEntries")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class GameEntry {
    @Column(name = "Id", nullable = false, unique = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "CreationTime", nullable = false)
    private long creationTime;
    @Transient
    private List<Long> invitations;
    @Transient
    private List<Long> members;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public List<Long> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Long> invitations) {
        this.invitations = invitations;
    }

    public List<Long> getMembers() {
        return members;
    }

    public void setMembers(List<Long> members) {
        this.members = members;
    }
}
