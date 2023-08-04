package org.dev.pixels.service.internal;

import org.dev.pixels.model.Account;
import org.dev.pixels.model.game.Player;
import org.dev.pixels.repository.PlayerRepository;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        private final AccountService accountService;

        public Inner(PlayerRepository playerRepository, AccountService accountService) {
            this.playerRepository = playerRepository;
            this.accountService = accountService;
        }

        public List<Player> readPlayers(Long id, Long accountId) {
            List<Player> players = null;
            if (id != null && id > 0) {
                players = new ArrayList<>();
                Player player = playerRepository.findById(id).orElse(null);
                if (player != null) {
                    players.add(player);
                }
            }
            if (accountId != null && accountId > 0) {
                if (players == null) {
                    players = new ArrayList<>();
                    Player player = playerRepository.findByAccountId(accountId).orElse(null);
                    if (player != null) {
                        players.add(player);
                    }
                }
                else {
                    players = players.stream().filter(p -> p.getAccountId() == accountId).collect(Collectors.toList());
                }
            }
            if (players == null) {
                throw new IllegalArgumentException();
            }
            return players;
        }

        public Player createPlayer(Player player) {
            player = clonePlayer(player);
            if (!validatePlayer(player)) {
                throw new IllegalArgumentException();
            }
            if (accountService.readAccounts(player.getAccountId(), null).isEmpty()) {
                throw new NotFoundException();
            }
            player.setId(0);
            return playerRepository.save(player);
        }

        public void handleDeleteAccount(Account deletedAccount) {
            playerRepository.deleteByAccountId(deletedAccount.getId());
        }

        private Player clonePlayer(Player player) {
            Player clone = new Player();
            clone.setId(player.getId());
            clone.setAccountId(player.getAccountId());
            return clone;
        }

        private boolean validatePlayer(Player player) {
            long accountId = player.getAccountId();
            if (accountId <= 0) {
                return false;
            }
            return true;
        }
    }
}
