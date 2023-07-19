package org.dev.commander.service;

import org.dev.commander.model.GameEntry;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotAuthorizedException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface GameService {
    List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException;
    GameEntry createGame(Authentication authentication, GameEntry gameEntry) throws NotAuthenticatedException, IllegalArgumentException;
    void leaveGame(Authentication authentication, long id);
}
