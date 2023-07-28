package org.dev.commander.service.internal;

import org.dev.commander.model.game.Action;
import org.dev.commander.model.game.GameState;

public interface GameService {
    void takeAction(long gameId, Action action);
    GameState getGameState(long gameId);
}
