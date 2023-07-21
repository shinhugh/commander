package org.dev.commander.service;

import org.dev.commander.model.Friendship;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface FriendshipService {
    List<Friendship> listFriendships(Authentication authentication) throws NotAuthenticatedException;
    void requestFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, ConflictException;
    void terminateFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException;
}
