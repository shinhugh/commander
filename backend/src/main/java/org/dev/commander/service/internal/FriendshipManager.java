package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.dev.commander.model.Friendship;
import org.dev.commander.model.Friendships;
import org.dev.commander.model.OutgoingMessage;
import org.dev.commander.repository.FriendshipRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.websocket.ObjectDispatcher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.lang.System.currentTimeMillis;

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
        Friendships friendships = inner.listFriendships(accountId);
        if (friendships.getConfirmedFriendships() != null) {
            for (Friendship friendship : friendships.getConfirmedFriendships()) {
                stripFieldsFromFriendship(friendship);
            }
        }
        if (friendships.getOutgoingRequests() != null) {
            for (Friendship friendship : friendships.getOutgoingRequests()) {
                stripFieldsFromFriendship(friendship);
            }
        }
        if (friendships.getIncomingRequests() != null) {
            for (Friendship friendship : friendships.getIncomingRequests()) {
                stripFieldsFromFriendship(friendship);
            }
        }
        return friendships;
    }

    @Override
    public void requestFriendship(long requestingAccountId, long respondingAccountId) throws IllegalArgumentException, NotFoundException, ConflictException {
        Set<Long> affectedAccountIds = inner.requestFriendship(requestingAccountId, respondingAccountId);
        OutgoingMessage<Void> message = new OutgoingMessage<>();
        message.setType(OutgoingMessage.Type.FRIENDSHIPS_CHANGE);
        for (long accountId : affectedAccountIds) {
            objectDispatcher.sendObject(accountId, message);
        }
    }

    @Override
    public void terminateFriendship(long accountAId, long accountBId) throws IllegalArgumentException, NotFoundException {
        Set<Long> affectedAccountIds = inner.terminateFriendship(accountAId, accountBId);
        OutgoingMessage<Void> message = new OutgoingMessage<>();
        message.setType(OutgoingMessage.Type.FRIENDSHIPS_CHANGE);
        for (long accountId : affectedAccountIds) {
            objectDispatcher.sendObject(accountId, message);
        }
    }

    @Override
    public void handleCreateAccount(Account newAccount) { }

    @Override
    public void handleUpdateAccount(Account preUpdateAccount, Account postUpdateAccount) { }

    @Override
    public void handleDeleteAccount(Account deletedAccount) {
        Set<Long> affectedAccountIds = inner.handleDeleteAccount(deletedAccount);
        OutgoingMessage<Void> message = new OutgoingMessage<>();
        message.setType(OutgoingMessage.Type.FRIENDSHIPS_CHANGE);
        for (long accountId : affectedAccountIds) {
            objectDispatcher.sendObject(accountId, message);
        }
    }

    private void stripFieldsFromFriendship(Friendship friendship) {
        friendship.setRequestingAccountId(null);
        friendship.setRespondingAccountId(null);
        friendship.setAccepted(null);
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
            if (accountId <= 0) {
                throw new IllegalArgumentException();
            }
            if (accountService.readAccounts(accountId, null).isEmpty()) {
                throw new NotFoundException();
            }
            Friendships friendships = new Friendships();
            friendships.setConfirmedFriendships(new ArrayList<>());
            friendships.setOutgoingRequests(new ArrayList<>());
            friendships.setIncomingRequests(new ArrayList<>());
            for (Friendship friendship : friendshipRepository.findByRequestingAccountIdOrRespondingAccountId(accountId, accountId)) {
                boolean accepted = friendship.getAccepted();
                boolean outgoing = Objects.equals(friendship.getRequestingAccountId(), accountId);
                long friendAccountId = outgoing ? friendship.getRespondingAccountId() : friendship.getRequestingAccountId();
                List<Account> accounts = accountService.readAccounts(friendAccountId, null);
                if (accounts.isEmpty()) {
                    friendshipRepository.delete(friendship);
                    continue;
                }
                friendship.setFriendAccount(accounts.get(0));
                if (accepted) {
                    friendships.getConfirmedFriendships().add(friendship);
                }
                else if (outgoing) {
                    friendships.getOutgoingRequests().add(friendship);
                } else {
                    friendships.getIncomingRequests().add(friendship);
                }
            }
            if (friendships.getConfirmedFriendships().isEmpty()) {
                friendships.setConfirmedFriendships(null);
            }
            if (friendships.getOutgoingRequests().isEmpty()) {
                friendships.setOutgoingRequests(null);
            }
            if (friendships.getIncomingRequests().isEmpty()) {
                friendships.setIncomingRequests(null);
            }
            return friendships;
        }

        public Set<Long> requestFriendship(long requestingAccountId, long respondingAccountId) {
            if (requestingAccountId <= 0 || respondingAccountId <= 0 || requestingAccountId == respondingAccountId) {
                throw new IllegalArgumentException();
            }
            Friendship.Key key = new Friendship.Key();
            key.setRequestingAccountId(requestingAccountId);
            key.setRespondingAccountId(respondingAccountId);
            Friendship friendship = friendshipRepository.findById(key).orElse(null);
            if (friendship == null) {
                key.setRequestingAccountId(respondingAccountId);
                key.setRespondingAccountId(requestingAccountId);
                friendship = friendshipRepository.findById(key).orElse(null);
            }
            if (friendship == null) {
                if (accountService.readAccounts(respondingAccountId, null).isEmpty()) {
                    throw new NotFoundException();
                }
                friendship = new Friendship();
                friendship.setRequestingAccountId(requestingAccountId);
                friendship.setRespondingAccountId(respondingAccountId);
                friendship.setAccepted(false);
                friendship.setCreationTime(currentTimeMillis());
                friendshipRepository.save(friendship);
                return Set.of(requestingAccountId, respondingAccountId);
            }
            if (!friendship.getAccepted() && Objects.equals(friendship.getRespondingAccountId(), requestingAccountId)) {
                friendship.setAccepted(true);
                return Set.of(requestingAccountId, respondingAccountId);
            }
            throw new ConflictException();
        }

        public Set<Long> terminateFriendship(long accountAId, long accountBId) {
            if (accountAId <= 0 || accountBId <= 0 || accountAId == accountBId) {
                throw new IllegalArgumentException();
            }
            Friendship.Key key = new Friendship.Key();
            key.setRequestingAccountId(accountAId);
            key.setRespondingAccountId(accountBId);
            Friendship friendship = friendshipRepository.findById(key).orElse(null);
            if (friendship == null) {
                key.setRequestingAccountId(accountBId);
                key.setRespondingAccountId(accountAId);
                friendship = friendshipRepository.findById(key).orElse(null);
            }
            if (friendship == null) {
                throw new NotFoundException();
            }
            friendshipRepository.delete(friendship);
            return Set.of(accountAId, accountBId);
        }

        public Set<Long> handleDeleteAccount(Account deletedAccount) {
            Set<Long> affectedAccountIds = new HashSet<>();
            List<Friendship> affectedFriendships = friendshipRepository.findByRequestingAccountIdOrRespondingAccountId(deletedAccount.getId(), deletedAccount.getId());
            for (Friendship friendship : affectedFriendships) {
                friendshipRepository.delete(friendship);
                affectedAccountIds.add(friendship.getRequestingAccountId());
                affectedAccountIds.add(friendship.getRespondingAccountId());
            }
            return affectedAccountIds;
        }
    }
}
