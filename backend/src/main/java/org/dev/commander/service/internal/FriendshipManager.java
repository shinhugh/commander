package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.model.Friendships;
import org.dev.commander.model.WebSocketMessage;
import org.dev.commander.repository.FriendshipRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.websocket.ObjectDispatcher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendshipManager implements FriendshipService, AccountEventHandler {
    private final Inner inner;
    private final ObjectDispatcher objectDispatcher;

    public FriendshipManager(Inner inner, ObjectDispatcher objectDispatcher, AccountService accountService) {
        this.inner = inner;
        this.objectDispatcher = objectDispatcher;
        accountService.registerAccountEventHandler(this);
    }

    @Override
    public Friendships listFriendships(long accountId) throws IllegalArgumentException, NotFoundException {
        return inner.listFriendships(accountId);
    }

    @Override
    public void requestFriendship(long requestingAccountId, long respondingAccountId) throws IllegalArgumentException, NotFoundException, ConflictException {
        inner.requestFriendship(requestingAccountId, respondingAccountId);
        // TODO: Catch JPA exception and throw ConflictException?
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
        objectDispatcher.sendObject(requestingAccountId, message);
        objectDispatcher.sendObject(respondingAccountId, message);
    }

    @Override
    public void terminateFriendship(long accountAId, long accountBId) throws IllegalArgumentException, NotFoundException {
        inner.terminateFriendship(accountAId, accountBId);
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
        objectDispatcher.sendObject(accountAId, message);
        objectDispatcher.sendObject(accountBId, message);
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
    public static class Inner {
        private final FriendshipRepository friendshipRepository;
        private final AccountService accountService;

        public Inner(FriendshipRepository friendshipRepository, AccountService accountService) {
            this.friendshipRepository = friendshipRepository;
            this.accountService = accountService;
        }

        public Friendships listFriendships(long accountId) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }

        public void requestFriendship(long requestingAccountId, long respondingAccountId) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }

        public void terminateFriendship(long accountAId, long accountBId) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }

        public void handleDeleteAccount(Account deletedAccount) {
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }
    }
}
