package org.dev.commander.model.game;

import java.util.Map;
import java.util.Set;

public class GameState {
    private Set<Player> players;
    private Space space;
    private Map<Long, Character> characters;

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Map<Long, Character> getCharacters() {
        return characters;
    }

    public void setCharacters(Map<Long, Character> characters) {
        this.characters = characters;
    }
}
