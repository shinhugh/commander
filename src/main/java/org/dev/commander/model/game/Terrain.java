package org.dev.commander.model.game;

public class Terrain {
    private Type type;
    private Player owner;
    private int hp;
    private boolean available;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public enum Type {
        MEADOW,
        FOREST,
        MOUNTAIN,
        ROAD,
        SEA,
        HQ,
        CITY,
        BARRACKS,
        FACTORY,
        SHIPYARD,
        AIRBASE
    }
}
