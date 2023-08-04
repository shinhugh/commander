package org.dev.pixels.service.internal;

import org.dev.pixels.model.Friendships;
import org.dev.pixels.service.exception.ConflictException;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotFoundException;

public interface FriendshipService {
    Friendships listFriendships(long accountId) throws IllegalArgumentException, NotFoundException;
    void requestFriendship(long requestingAccountId, long respondingAccountId) throws IllegalArgumentException, NotFoundException, ConflictException;
    void terminateFriendship(long accountAId, long accountBId) throws IllegalArgumentException, NotFoundException;
}
