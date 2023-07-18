package org.dev.commander.repository;

import org.dev.commander.model.GameParticipation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameParticipationRepository extends CrudRepository<GameParticipation, GameParticipation.Key> {
    List<GameParticipation> findByGameEntryId(long gameEntryId);
    List<GameParticipation> findByAccountId(long accountId);
}
