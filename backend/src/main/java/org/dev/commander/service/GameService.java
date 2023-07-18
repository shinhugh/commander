package org.dev.commander.service;

import org.dev.commander.model.GameEntry;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface GameService {
    List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id);
    GameEntry createGame(Authentication authentication, GameEntry gameEntry);
    void leaveGame(Authentication authentication, long id);
}
