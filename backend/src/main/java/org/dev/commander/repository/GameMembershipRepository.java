package org.dev.commander.repository;

import org.dev.commander.model.GameMembership;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameMembershipRepository extends CrudRepository<GameMembership, GameMembership.Key> {
    List<GameMembership> findByGameEntryId(Long gameEntryId);
    List<GameMembership> findByAccountId(Long accountId);
}
