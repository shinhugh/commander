package org.dev.commander.repository;

import org.dev.commander.model.GameMembership;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameMembershipRepository extends CrudRepository<GameMembership, GameMembership.Key> {
    List<GameMembership> findByGameEntryId(long gameEntryId);
    List<GameMembership> findByAccountId(long accountId);
}
