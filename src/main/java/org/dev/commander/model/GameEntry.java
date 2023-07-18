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
    @Transient
    private List<Long> invitedPlayers;
    @Transient
    private List<Long> participatingPlayers;
    @Transient
    private long creationTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Long> getInvitedPlayers() {
        return invitedPlayers;
    }

    public void setInvitedPlayers(List<Long> invitedPlayers) {
        this.invitedPlayers = invitedPlayers;
    }

    public List<Long> getParticipatingPlayers() {
        return participatingPlayers;
    }

    public void setParticipatingPlayers(List<Long> participatingPlayers) {
        this.participatingPlayers = participatingPlayers;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
