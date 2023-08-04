package org.dev.pixels.repository;

import org.dev.pixels.model.Friendship;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendshipRepository extends CrudRepository<Friendship, Friendship.Key> {
    List<Friendship> findByRequestingAccountIdOrRespondingAccountId(Long requestingAccountId, Long respondingAccountId);
    void deleteByRequestingAccountIdOrRespondingAccountId(Long requestingAccountId, Long respondingAccountId);
}
