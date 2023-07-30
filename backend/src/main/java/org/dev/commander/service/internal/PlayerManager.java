package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.model.game.Player;
import org.dev.commander.repository.PlayerRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerManager implements PlayerService, AccountEventHandler {
    private final Inner inner;

    public PlayerManager(Inner inner, AccountService accountService) {
        this.inner = inner;
        accountService.registerAccountEventHandler(this);
    }

    @Override
    public List<Player> readPlayers(Long id, Long accountId) {
        return inner.readPlayers(id, accountId);
    }

    @Override
    public Player createPlayer(Player player) {
        return inner.createPlayer(player);
    }

    @Override
    public void handleCreateAccount(Account newAccount) { }

    @Override
    public void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) { }

    @Override
    public void handleDeleteAccount(Account deletedAccount) {
        inner.handleDeleteAccount(deletedAccount);
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private static class Inner {
        private final PlayerRepository playerRepository;

        public Inner(PlayerRepository playerRepository) {
            this.playerRepository = playerRepository;
        }

        public List<Player> readPlayers(Long id, Long accountId) {
            // TODO: Read players
            throw new RuntimeException("Not implemented");
        }

        public Player createPlayer(Player player) {
            // TODO: Create player
            throw new RuntimeException("Not implemented");
        }

        public void handleDeleteAccount(Account deletedAccount) {
            // TODO: Delete corresponding player
            throw new RuntimeException("Not implemented");
        }
    }
}
