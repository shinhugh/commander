package org.dev.commander.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "GameEntries")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameEntry {
    @Column(name = "Id", nullable = false, unique = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "CreationTime", nullable = false)
    private Long creationTime;
    @Transient
    private List<Long> invitations;
    @Transient
    private List<Long> members;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
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
