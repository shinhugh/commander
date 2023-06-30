package org.dev.commander.repository;

import org.dev.commander.model.game.Game;

public interface GameRepository {
    Game readGameById(long id);
    Game createGame(Game game);
    Game updateGameById(long id, Game game);
    void deleteGameById(long id);
}
