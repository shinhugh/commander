package org.dev.commander.model.game;

import java.util.List;

public class Move {
    private int originY;
    private int originX;
    private Unit.Type create;
    private List<Direction> travel;
    private int attackY;
    private int attackX;
    private Direction embark;
    private boolean capture;
    private boolean submerge;

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        this.originY = originY;
    }

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        this.originX = originX;
    }

    public Unit.Type getCreate() {
        return create;
    }

    public void setCreate(Unit.Type create) {
        this.create = create;
    }

    public List<Direction> getTravel() {
        return travel;
    }

    public void setTravel(List<Direction> travel) {
        this.travel = travel;
    }

    public int getAttackY() {
        return attackY;
    }

    public void setAttackY(int attackY) {
        this.attackY = attackY;
    }

    public int getAttackX() {
        return attackX;
    }

    public void setAttackX(int attackX) {
        this.attackX = attackX;
    }

    public Direction getEmbark() {
        return embark;
    }

    public void setEmbark(Direction embark) {
        this.embark = embark;
    }

    public boolean isCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }

    public boolean isSubmerge() {
        return submerge;
    }

    public void setSubmerge(boolean submerge) {
        this.submerge = submerge;
    }

    public enum Direction {
        RIGHT,
        DOWN,
        LEFT,
        UP
    }
}
