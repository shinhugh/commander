package org.dev.commander.repository;

import org.dev.commander.model.Friendship;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendshipRepository extends CrudRepository<Friendship, Friendship.Key> {
    List<Friendship> findByAccountAIdOrAccountBId(Long accountAId, Long accountBId);
}
