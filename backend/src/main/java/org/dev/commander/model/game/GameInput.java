package org.dev.commander.model.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameInput {
    private long playerId;
    private Type type;
    private Double posX;
    private Double posY;
    private Direction orientation;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Double getPosX() {
        return posX;
    }

    public void setPosX(Double posX) {
        this.posX = posX;
    }

    public Double getPosY() {
        return posY;
    }

    public void setPosY(Double posY) {
        this.posY = posY;
    }

    public Direction getOrientation() {
        return orientation;
    }

    public void setOrientation(Direction orientation) {
        this.orientation = orientation;
    }

    public enum Type {
        @JsonProperty("join")
        JOIN,
        @JsonProperty("leave")
        LEAVE,
        @JsonProperty("position")
        POSITION
    }
}
