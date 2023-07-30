package org.dev.commander.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Character {
    private long playerId;
    private double width;
    private double height;
    private double posX;
    private double posY;
    @JsonIgnore
    private Direction pendingMovementDirection;
    @JsonIgnore
    private long pendingMovementDuration;
    private Direction orientationDirection;
    @JsonIgnore
    private double movementSpeed;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public Direction getPendingMovementDirection() {
        return pendingMovementDirection;
    }

    public void setPendingMovementDirection(Direction pendingMovementDirection) {
        this.pendingMovementDirection = pendingMovementDirection;
    }

    public long getPendingMovementDuration() {
        return pendingMovementDuration;
    }

    public void setPendingMovementDuration(long pendingMovementDuration) {
        this.pendingMovementDuration = pendingMovementDuration;
    }

    public Direction getOrientationDirection() {
        return orientationDirection;
    }

    public void setOrientationDirection(Direction orientationDirection) {
        this.orientationDirection = orientationDirection;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}
