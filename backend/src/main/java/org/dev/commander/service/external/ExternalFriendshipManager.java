package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.model.Friendship;
import org.dev.commander.model.Friendships;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.service.internal.FriendshipService;
import org.dev.commander.service.internal.IdentificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ExternalFriendshipManager implements ExternalFriendshipService {
    private final FriendshipService friendshipService;
    private final IdentificationService identificationService;

    public ExternalFriendshipManager(FriendshipService friendshipService, IdentificationService identificationService) {
        this.friendshipService = friendshipService;
        this.identificationService = identificationService;
    }

    @Override
    public Friendships listFriendships(Authentication authentication) throws NotAuthenticatedException {
        Account clientAccount = identificationService.identifyAccount(authentication);
        if (clientAccount == null) {
            throw new NotAuthenticatedException();
        }
        Friendships friendships = friendshipService.listFriendships(clientAccount.getId());
        if (friendships.getConfirmedFriendships() != null) {
            for (Friendship friendship : friendships.getConfirmedFriendships()) {
                stripFieldsFromAccount(friendship.getFriendAccount());
            }
        }
        if (friendships.getOutgoingRequests() != null) {
            for (Friendship friendship : friendships.getOutgoingRequests()) {
                stripFieldsFromAccount(friendship.getFriendAccount());
            }
        }
        if (friendships.getIncomingRequests() != null) {
            for (Friendship friendship : friendships.getIncomingRequests()) {
                stripFieldsFromAccount(friendship.getFriendAccount());
            }
        }
        return friendships;
    }

    @Override
    public void requestFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, ConflictException {
        Account clientAccount = identificationService.identifyAccount(authentication);
        if (clientAccount == null) {
            throw new NotAuthenticatedException();
        }
        if (accountId == null) {
            throw new IllegalArgumentException();
        }
        friendshipService.requestFriendship(clientAccount.getId(), accountId);
    }

    @Override
    public void terminateFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException {
        Account clientAccount = identificationService.identifyAccount(authentication);
        if (clientAccount == null) {
            throw new NotAuthenticatedException();
        }
        if (accountId == null) {
            throw new IllegalArgumentException();
        }
        friendshipService.terminateFriendship(clientAccount.getId(), accountId);
    }

    private void stripFieldsFromAccount(Account account) {
        account.setLoginName(null);
        account.setPassword(null);
        account.setAuthorities(null);
    }
}
