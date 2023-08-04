package org.dev.pixels.repository;

import org.dev.pixels.model.game.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Long> {
    Optional<Player> findByAccountId(long accountId);
    void deleteByAccountId(long accountId);
}
