package org.dev.commander.model.game;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameState {
    private long clientPlayerId;
    private Space space;
    private Map<Long, Character> characters;

    public long getClientPlayerId() {
        return clientPlayerId;
    }

    public void setClientPlayerId(long clientPlayerId) {
        this.clientPlayerId = clientPlayerId;
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
