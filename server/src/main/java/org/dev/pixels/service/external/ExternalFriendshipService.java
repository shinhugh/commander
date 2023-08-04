package org.dev.pixels.service.external;

import org.dev.pixels.model.Friendships;
import org.dev.pixels.service.exception.ConflictException;
import org.dev.pixels.service.exception.NotAuthenticatedException;
import org.dev.pixels.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;

public interface ExternalFriendshipService {
    Friendships listFriendships(Authentication authentication) throws NotAuthenticatedException;
    void requestFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, ConflictException;
    void terminateFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException;
}
