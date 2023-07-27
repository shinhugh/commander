package org.dev.commander.service.internal;

import org.dev.commander.model.Friendships;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;

public interface FriendshipService {
    Friendships listFriendships(long accountId) throws IllegalArgumentException, NotFoundException;
    void requestFriendship(long requestingAccountId, long respondingAccountId) throws IllegalArgumentException, NotFoundException, ConflictException;
    void terminateFriendship(long accountAId, long accountBId) throws IllegalArgumentException, NotFoundException;
}
