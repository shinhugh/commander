package org.dev.pixels.service.external;

import org.dev.pixels.model.Account;
import org.dev.pixels.model.Friendship;
import org.dev.pixels.model.Friendships;
import org.dev.pixels.service.exception.ConflictException;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotAuthenticatedException;
import org.dev.pixels.service.exception.NotFoundException;
import org.dev.pixels.service.internal.FriendshipService;
import org.dev.pixels.service.internal.IdentificationService;
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
