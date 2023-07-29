package org.dev.commander.model.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Direction {
    @JsonProperty("up")
    UP,
    @JsonProperty("up_right")
    UP_RIGHT,
    @JsonProperty("right")
    RIGHT,
    @JsonProperty("down_right")
    DOWN_RIGHT,
    @JsonProperty("down")
    DOWN,
    @JsonProperty("down_left")
    DOWN_LEFT,
    @JsonProperty("left")
    LEFT,
    @JsonProperty("up_left")
    UP_LEFT
}
