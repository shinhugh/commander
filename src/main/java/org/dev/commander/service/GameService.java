package org.dev.commander.service;

import org.dev.commander.model.Game;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface GameService {
    List<Long> readGameIds(Authentication authentication, long accountId);
    Game readGame(Authentication authentication, long id);
    Game createGame(Authentication authentication, Game game);
    void leaveGame(Authentication authentication, long id);
}
