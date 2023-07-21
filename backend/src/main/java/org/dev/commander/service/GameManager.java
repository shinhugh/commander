package org.dev.commander.service;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Account;
import org.dev.commander.model.GameEntry;
import org.dev.commander.model.GameInvitation;
import org.dev.commander.model.GameMembership;
import org.dev.commander.repository.AccountRepository;
import org.dev.commander.repository.GameEntryRepository;
import org.dev.commander.repository.GameInvitationRepository;
import org.dev.commander.repository.GameMembershipRepository;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotAuthorizedException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.websocket.ObjectDispatcher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.System.currentTimeMillis;

@Service
public class GameManager implements GameService {
    private final Inner inner;

    public GameManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, NotAuthorizedException {
        return inner.readGameEntries(authentication, accountId, id);
    }

    @Override
    public GameEntry createGame(Authentication authentication, GameEntry gameEntry) throws NotAuthenticatedException, IllegalArgumentException {
        return inner.createGame(authentication, gameEntry);
    }

    @Override
    public void leaveGame(Authentication authentication, Long id) {
        inner.leaveGame(authentication, id);
    }

    @Component
    @Transactional
    public static class Inner {
        private static final long INVITATION_LIFETIME = 86400000L;
        private final GameEntryRepository gameEntryRepository;
        private final GameInvitationRepository gameInvitationRepository;
        private final GameMembershipRepository gameMembershipRepository;
        private final AccountRepository accountRepository;
        private final AuthorizationService authorizationService;
        private final ObjectDispatcher objectDispatcher;

        public Inner(GameEntryRepository gameEntryRepository, GameInvitationRepository gameInvitationRepository, GameMembershipRepository gameMembershipRepository, AccountRepository accountRepository, AuthorizationService authorizationService, ObjectDispatcher objectDispatcher) {
            this.gameEntryRepository = gameEntryRepository;
            this.gameInvitationRepository = gameInvitationRepository;
            this.gameMembershipRepository = gameMembershipRepository;
            this.accountRepository = accountRepository;
            this.authorizationService = authorizationService;
            this.objectDispatcher = objectDispatcher;
        }

        public List<GameEntry> readGameEntries(Authentication authentication, Long accountId, Long id) {
            Account clientAccount = authorizationService.getAccount(authentication);
            if (clientAccount == null) {
                throw new NotAuthenticatedException();
            }
            boolean isAdmin = authorizationService.verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"));
            if (id != null) {
                GameEntry gameEntry = gameEntryRepository.findById(id).orElse(null);
                if (accountId != null && gameEntry != null) {
                    List<GameMembership> gameMemberships = gameMembershipRepository.findByGameEntryId(gameEntry.getId());
                    if (gameMemberships.stream().noneMatch(p -> p.getAccountId() == accountId)) {
                        gameEntry = null;
                    }
                }
                if (gameEntry == null) {
                    if (isAdmin) {
                        throw new NotFoundException();
                    }
                    throw new NotAuthorizedException();
                }
                List<GameMembership> gameMemberships = gameMembershipRepository.findByGameEntryId(id);
                if (!isAdmin && gameMemberships.stream().noneMatch(p -> p.getAccountId() == clientAccount.getId())) {
                    throw new NotAuthorizedException();
                }
                List<Long> invitations = new ArrayList<>();
                List<GameInvitation> gameInvitations = gameInvitationRepository.findByGameEntryId(id);
                for (GameInvitation gameInvitation : gameInvitations) {
                    invitations.add(gameInvitation.getAccountId());
                }
                gameEntry.setInvitations(invitations);
                List<Long> members = new ArrayList<>();
                for (GameMembership gameMembership : gameMemberships) {
                    members.add(gameMembership.getAccountId());
                }
                gameEntry.setMembers(members);
                return List.of(gameEntry);
            }
            if (accountId != null) {
                if (!isAdmin && accountId != clientAccount.getId()) {
                    throw new NotAuthorizedException();
                }
                List<GameEntry> gameEntries = new ArrayList<>();
                List<GameMembership> accountGameMemberships = gameMembershipRepository.findByAccountId(accountId);
                for (GameMembership accountGameMembership : accountGameMemberships) {
                    GameEntry gameEntry = gameEntryRepository.findById(accountGameMembership.getGameEntryId()).orElse(null);
                    if (gameEntry == null) {
                        continue;
                    }
                    List<Long> invitations = new ArrayList<>();
                    List<GameInvitation> gameInvitations = gameInvitationRepository.findByGameEntryId(gameEntry.getId());
                    for (GameInvitation gameInvitation : gameInvitations) {
                        invitations.add(gameInvitation.getAccountId());
                    }
                    gameEntry.setInvitations(invitations);
                    List<Long> members = new ArrayList<>();
                    List<GameMembership> gameMemberships = gameMembershipRepository.findByGameEntryId(gameEntry.getId());
                    for (GameMembership gameMembership : gameMemberships) {
                        members.add(gameMembership.getAccountId());
                    }
                    gameEntry.setMembers(members);
                    gameEntries.add(gameEntry);
                }
                return gameEntries;
            }
            throw new IllegalArgumentException();
        }

        public GameEntry createGame(Authentication authentication, GameEntry gameEntry) {
            Account clientAccount = authorizationService.getAccount(authentication);
            if (clientAccount == null) {
                throw new NotAuthenticatedException();
            }
            if (gameEntry.getInvitations() == null || gameEntry.getInvitations().isEmpty()) {
                throw new IllegalArgumentException();
            }
            long creationTime = currentTimeMillis();
            long invitationExpirationTime = creationTime + INVITATION_LIFETIME;
            GameEntry newGameEntry = new GameEntry();
            newGameEntry.setCreationTime(creationTime);
            newGameEntry = gameEntryRepository.save(newGameEntry);
            GameMembership newGameMembership = new GameMembership();
            newGameMembership.setGameEntryId(newGameEntry.getId());
            newGameMembership.setAccountId(clientAccount.getId());
            gameMembershipRepository.save(newGameMembership);
            List<Long> gameInvitations = new ArrayList<>();
            for (Long accountId : gameEntry.getInvitations()) {
                if (accountId == null) {
                    continue;
                }
                if (!accountRepository.existsById(accountId)) {
                    throw new IllegalArgumentException();
                }
                GameInvitation newGameInvitation = new GameInvitation();
                newGameInvitation.setGameEntryId(newGameEntry.getId());
                newGameInvitation.setAccountId(accountId);
                newGameInvitation.setCreationTime(creationTime);
                newGameInvitation.setExpirationTime(invitationExpirationTime);
                gameInvitationRepository.save(newGameInvitation);
                gameInvitations.add(accountId);
            }
            if (gameInvitations.isEmpty()) {
                throw new IllegalArgumentException();
            }
            newGameEntry.setInvitations(gameInvitations);
            newGameEntry.setMembers(List.of(clientAccount.getId()));
            // TODO: Notify invited players via WebSocket
            return newGameEntry;
        }

        public void leaveGame(Authentication authentication, long id) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }
    }
}
