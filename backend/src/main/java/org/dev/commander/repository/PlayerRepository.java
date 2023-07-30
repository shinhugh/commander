package org.dev.commander.repository;

import org.dev.commander.model.game.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Optional<Player> findByAccountId(long accountId);
}
