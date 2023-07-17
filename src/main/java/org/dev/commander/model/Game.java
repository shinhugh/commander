package org.dev.commander.model;

import java.util.List;

public class Game {
    private long id;
    private List<Long> players;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Long> getPlayers() {
        return players;
    }

    public void setPlayers(List<Long> players) {
        this.players = players;
    }
}
