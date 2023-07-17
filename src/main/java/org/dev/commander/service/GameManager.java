package org.dev.commander.service;

import org.dev.commander.model.Game;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameManager implements GameService {
    @Override
    public List<Game> readGames(Authentication authentication, Long accountId, Long id) {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Game createGame(Authentication authentication, Game game) {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void leaveGame(Authentication authentication, long id) {
        // TODO
        throw new RuntimeException("Not implemented");
    }
}
