package org.dev.commander.model.game;

import java.util.List;

public class Action {
    private long actorUnitId;
    private List<Move> moves;
    private Operation operation;
    private long targetFacilityId;
    private long targetUnitId;

    public long getActorUnitId() {
        return actorUnitId;
    }

    public void setActorUnitId(long actorUnitId) {
        this.actorUnitId = actorUnitId;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public long getTargetFacilityId() {
        return targetFacilityId;
    }

    public void setTargetFacilityId(long targetFacilityId) {
        this.targetFacilityId = targetFacilityId;
    }

    public long getTargetUnitId() {
        return targetUnitId;
    }

    public void setTargetUnitId(long targetUnitId) {
        this.targetUnitId = targetUnitId;
    }

    public static class Move {
        private int distance;
        private Direction direction;

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public enum Direction {
            UP,
            RIGHT,
            DOWN,
            LEFT
        }
    }

    public enum Operation {
        CAPTURE,
        EMBARK,
        ATTACK
    }
}
