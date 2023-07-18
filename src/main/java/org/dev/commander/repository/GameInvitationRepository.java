package org.dev.commander.repository;

import org.dev.commander.model.GameInvitation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameInvitationRepository extends CrudRepository<GameInvitation, GameInvitation.Key> {
    List<GameInvitation> findByGameEntryId(long gameEntryId);
    List<GameInvitation> findByAccountId(long accountId);
}
