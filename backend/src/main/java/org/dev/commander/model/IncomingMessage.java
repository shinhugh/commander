package org.dev.commander.model;

public class IncomingMessage<T> {
    private Type type;
    private T payload;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public enum Type {
        GAME_ACTION
    }
}
