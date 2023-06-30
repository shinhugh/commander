package org.dev.commander.model.game;

public class Unit {
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
        GRUNT,
        GROUND_CARRIER,
        RECON,
        TANK,
        AA_MISSILE_LAUNCHER,
        ROCKET_LAUNCHER,
        SEA_CARRIER,
        CRUISER,
        BATTLESHIP,
        SUBMARINE,
        AIR_CARRIER,
        ATTACK_HELICOPTER,
        JET,
        BOMBER
    }
}
