package org.dev.pixels.service.internal;

import org.dev.pixels.model.game.Player;

import java.util.List;

public interface PlayerService {
    List<Player> readPlayers(Long id, Long accountId);
    Player createPlayer(Player player);
}
