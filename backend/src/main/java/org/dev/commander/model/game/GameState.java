package org.dev.commander.model.game;

import java.util.List;
import java.util.Map;

public class GameState {
    private long id;
    private Map<Long, Player> players;
    private Space space;
    private List<List<Terrain>> terrain;
    private Map<Long, Facility> facilities;
    private Map<Long, Unit> units;
    private List<Action> actionLog;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<Long, Player> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Long, Player> players) {
        this.players = players;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public List<List<Terrain>> getTerrain() {
        return terrain;
    }

    public void setTerrain(List<List<Terrain>> terrain) {
        this.terrain = terrain;
    }

    public Map<Long, Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(Map<Long, Facility> facilities) {
        this.facilities = facilities;
    }

    public Map<Long, Unit> getUnits() {
        return units;
    }

    public void setUnits(Map<Long, Unit> units) {
        this.units = units;
    }

    public List<Action> getActionLog() {
        return actionLog;
    }

    public void setActionLog(List<Action> actionLog) {
        this.actionLog = actionLog;
    }
}
