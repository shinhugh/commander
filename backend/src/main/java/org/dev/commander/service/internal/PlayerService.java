package org.dev.commander.service.internal;

import org.dev.commander.model.game.Player;

import java.util.List;

public interface PlayerService {
    List<Player> readPlayers(Long id, Long accountId);
    Player createPlayer(Player player);
}
