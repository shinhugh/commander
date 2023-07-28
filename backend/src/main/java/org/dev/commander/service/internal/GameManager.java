package org.dev.commander.service.internal;

import org.dev.commander.model.game.Action;
import org.dev.commander.model.game.GameState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameManager implements GameService {
    private final Map<Long, GameState> gameStates = new HashMap<>();

    // For testing purposes only
    public GameManager() {
        gameStates.put(1L, generateMockGameState());
    }

    @Override
    public void takeAction(long gameId, Action action) {
        GameState gameState = gameStates.get(gameId);
    }

    @Override
    public GameState getGameState(long gameId) {
        return gameStates.get(gameId);
    }

    // For testing purposes only
    private GameState generateMockGameState() {
        GameState gameState = new GameState();
        return gameState;
    }
}
