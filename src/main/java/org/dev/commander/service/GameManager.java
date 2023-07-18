package org.dev.commander.service;

import jakarta.transaction.Transactional;
import org.dev.commander.model.GameEntry;
import org.dev.commander.model.GameParticipation;
import org.dev.commander.repository.GameEntryRepository;
import org.dev.commander.repository.GameInvitationRepository;
import org.dev.commander.repository.GameParticipationRepository;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotAuthorizedException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GameManager implements GameService {
    private final Inner inner;

    public GameManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id) {
        return inner.readGameEntries(authentication, accountId, id);
    }

    @Override
    public GameEntry createGame(Authentication authentication, GameEntry gameEntry) {
        return inner.createGame(authentication, gameEntry);
    }

    @Override
    public void leaveGame(Authentication authentication, long id) {
        inner.leaveGame(authentication, id);
    }

    @Component
    @Transactional
    public static class Inner {
        private final GameEntryRepository gameEntryRepository;
        private final GameInvitationRepository gameInvitationRepository;
        private final GameParticipationRepository gameParticipationRepository;
        private final AuthorityVerificationService authorityVerificationService;

        public Inner(GameEntryRepository gameEntryRepository, GameInvitationRepository gameInvitationRepository, GameParticipationRepository gameParticipationRepository, AuthorityVerificationService authorityVerificationService) {
            this.gameEntryRepository = gameEntryRepository;
            this.gameInvitationRepository = gameInvitationRepository;
            this.gameParticipationRepository = gameParticipationRepository;
            this.authorityVerificationService = authorityVerificationService;
        }

        public List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id) {
            if (authentication == null) {
                throw new NotAuthenticatedException();
            }
            long clientAccountId = authorityVerificationService.getAccountId(authentication);
            boolean isAdmin = authorityVerificationService.verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"));
            if (id != null) {
                GameEntry gameEntry = gameEntryRepository.findById(id).orElse(null);
                if (accountId != null && gameEntry != null) {
                    List<GameParticipation> participatingGames = gameParticipationRepository.findByGameEntryId(gameEntry.getId());
                    if (participatingGames.stream().noneMatch(p -> p.getAccountId() == accountId)) {
                        gameEntry = null;
                    }
                }
                if (gameEntry == null) {
                    if (isAdmin) {
                        throw new NotFoundException();
                    }
                    throw new NotAuthorizedException();
                }
                if (!isAdmin && gameParticipationRepository.findByGameEntryId(id).stream().noneMatch(p -> p.getAccountId() == clientAccountId)) {
                    throw new NotAuthorizedException();
                }
                return List.of(gameEntry);
            }
            if (accountId != null) {
                if (!isAdmin && accountId != clientAccountId) {
                    throw new NotAuthorizedException();
                }
                List<GameEntry> gameEntries = new ArrayList<>();
                List<GameParticipation> participatingGames = gameParticipationRepository.findByAccountId(accountId);
                for (GameParticipation participatingGame : participatingGames) {
                    gameEntryRepository.findById(participatingGame.getGameEntryId()).ifPresent(gameEntries::add);
                }
                return gameEntries;
            }
            throw new IllegalArgumentException();
        }

        public GameEntry createGame(Authentication authentication, GameEntry gameEntry) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }

        public void leaveGame(Authentication authentication, long id) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }
    }
}
