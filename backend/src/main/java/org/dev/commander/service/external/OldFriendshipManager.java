package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.model.Friendship;
import org.dev.commander.model.Friendships;
import org.dev.commander.model.WebSocketMessage;
import org.dev.commander.repository.FriendshipRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.service.internal.AccountService;
import org.dev.commander.service.internal.IdentificationService;
import org.dev.commander.websocket.ObjectDispatcher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

// TODO: Delete
public class OldFriendshipManager {
//    private final Inner inner;
//
//    public OldFriendshipManager(Inner inner) {
//        this.inner = inner;
//    }
//
//    public Friendships listFriendships(Authentication authentication) throws NotAuthenticatedException {
//        return inner.listFriendships(authentication);
//    }
//
//    public void requestFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, ConflictException {
//        inner.requestFriendship(authentication, accountId);
//    }
//
//    public void terminateFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException {
//        inner.terminateFriendship(authentication, accountId);
//    }
//
//    public void terminateAllFriendships(Authentication authentication) throws NotAuthenticatedException {
//        inner.terminateAllFriendships(authentication);
//    }
//
//    @Component
//    @Transactional
//    public static class Inner {
//        private final FriendshipRepository friendshipRepository;
//        private final AccountService accountService;
//        private final IdentificationService identificationService;
//        private final ObjectDispatcher objectDispatcher;
//
//        public Inner(FriendshipRepository friendshipRepository, AccountService accountService, IdentificationService identificationService, ObjectDispatcher objectDispatcher) {
//            this.friendshipRepository = friendshipRepository;
//            this.accountService = accountService;
//            this.identificationService = identificationService;
//            this.objectDispatcher = objectDispatcher;
//            // TODO: Subscribe to account delete and handle by deleting friendships and notifying friends via WebSocket
//        }
//
//        public Friendships listFriendships(Authentication authentication) {
//            Account clientAccount = identificationService.identifyAccount(authentication);
//            if (clientAccount == null) {
//                throw new NotAuthenticatedException();
//            }
//            Friendships friendships = new Friendships();
//            friendships.setConfirmedFriendships(new ArrayList<>());
//            friendships.setOutgoingRequests(new ArrayList<>());
//            friendships.setIncomingRequests(new ArrayList<>());
//            List<Friendship> friendshipList = friendshipRepository.findByRequestingAccountIdOrRespondingAccountId(clientAccount.getId(), clientAccount.getId());
//            for (Iterator<Friendship> it = friendshipList.iterator(); it.hasNext();) {
//                Friendship friendship = it.next();
//                boolean accepted = friendship.getAccepted();
//                boolean outgoing = Objects.equals(friendship.getRequestingAccountId(), clientAccount.getId());
//                long friendAccountId = outgoing ? friendship.getRespondingAccountId() : friendship.getRequestingAccountId();
//                Account friendAccount;
//                try {
//                    friendAccount = accountService.readAccounts(authentication, friendAccountId);
//                }
//                catch (NotFoundException ex) {
//                    it.remove();
//                    continue;
//                }
//                friendship = cloneFriendship(friendship);
//                friendship.setRequestingAccountId(null);
//                friendship.setRespondingAccountId(null);
//                friendship.setAccepted(null);
//                friendship.setFriendAccount(friendAccount);
//                if (accepted) {
//                    friendships.getConfirmedFriendships().add(friendship);
//                    continue;
//                }
//                if (outgoing) {
//                    friendships.getOutgoingRequests().add(friendship);
//                    continue;
//                }
//                friendships.getIncomingRequests().add(friendship);
//            }
//            if (friendships.getConfirmedFriendships().isEmpty()) {
//                friendships.setConfirmedFriendships(null);
//            }
//            if (friendships.getOutgoingRequests().isEmpty()) {
//                friendships.setOutgoingRequests(null);
//            }
//            if (friendships.getIncomingRequests().isEmpty()) {
//                friendships.setIncomingRequests(null);
//            }
//            return friendships;
//        }
//
//        public void requestFriendship(Authentication authentication, Long accountId) {
//            Account clientAccount = identificationService.identifyAccount(authentication);
//            if (clientAccount == null) {
//                throw new NotAuthenticatedException();
//            }
//            if (accountId == null || accountId <= 0 || accountId.equals(clientAccount.getId())) {
//                throw new IllegalArgumentException();
//            }
//            Friendship.Key key = new Friendship.Key();
//            key.setRequestingAccountId(clientAccount.getId());
//            key.setRespondingAccountId(accountId);
//            Friendship friendship = friendshipRepository.findById(key).orElse(null);
//            if (friendship == null) {
//                key.setRequestingAccountId(accountId);
//                key.setRespondingAccountId(clientAccount.getId());
//                friendship = friendshipRepository.findById(key).orElse(null);
//            }
//            if (friendship == null) {
//                accountService.readAccounts(authentication, accountId);
//                friendship = new Friendship();
//                friendship.setRequestingAccountId(clientAccount.getId());
//                friendship.setRespondingAccountId(accountId);
//                friendship.setAccepted(false);
//                friendship.setCreationTime(currentTimeMillis());
//                friendshipRepository.save(friendship);
//                // TODO: Will message go out before save happens (because of @Transactional)? (It should not)
//                WebSocketMessage message = new WebSocketMessage();
//                message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
//                objectDispatcher.sendObject(clientAccount.getId(), message);
//                objectDispatcher.sendObject(accountId, message);
//                return;
//            }
//            if (!friendship.getAccepted() && Objects.equals(friendship.getRespondingAccountId(), clientAccount.getId())) {
//                friendship.setAccepted(true);
//                // TODO: Will message go out before modification happens (because of @Transactional)? (It should not)
//                WebSocketMessage message = new WebSocketMessage();
//                message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
//                objectDispatcher.sendObject(clientAccount.getId(), message);
//                objectDispatcher.sendObject(accountId, message);
//                return;
//            }
//            throw new ConflictException();
//        }
//
//        public void terminateFriendship(Authentication authentication, Long accountId) {
//            Account clientAccount = identificationService.identifyAccount(authentication);
//            if (clientAccount == null) {
//                throw new NotAuthenticatedException();
//            }
//            if (accountId == null || accountId <= 0 || accountId.equals(clientAccount.getId())) {
//                throw new IllegalArgumentException();
//            }
//            Friendship.Key key = new Friendship.Key();
//            key.setRequestingAccountId(clientAccount.getId());
//            key.setRespondingAccountId(accountId);
//            Friendship friendship = friendshipRepository.findById(key).orElse(null);
//            if (friendship == null) {
//                key.setRequestingAccountId(accountId);
//                key.setRespondingAccountId(clientAccount.getId());
//                friendship = friendshipRepository.findById(key).orElse(null);
//            }
//            if (friendship == null) {
//                throw new NotFoundException();
//            }
//            friendshipRepository.delete(friendship);
//            // TODO: Will message go out before delete happens (because of @Transactional)? (It should not)
//            WebSocketMessage message = new WebSocketMessage();
//            message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
//            objectDispatcher.sendObject(clientAccount.getId(), message);
//            objectDispatcher.sendObject(accountId, message);
//        }
//
//        public void terminateAllFriendships(Authentication authentication) {
//            Account clientAccount = identificationService.identifyAccount(authentication);
//            if (clientAccount == null) {
//                throw new NotAuthenticatedException();
//            }
//            List<Friendship> friendships = friendshipRepository.findByRequestingAccountIdOrRespondingAccountId(clientAccount.getId(), clientAccount.getId());
//            List<Long> friendIds = new ArrayList<>();
//            for (Friendship friendship : friendships) {
//                friendIds.add(friendship.getFriendAccount().getId());
//            }
//            friendshipRepository.deleteByRequestingAccountIdOrRespondingAccountId(clientAccount.getId(), clientAccount.getId());
//            // TODO: Will message go out before deletes happen (because of @Transactional)? (It should not)
//            WebSocketMessage message = new WebSocketMessage();
//            message.setType(WebSocketMessage.Type.FRIENDSHIPS_CHANGE);
//            objectDispatcher.sendObject(clientAccount.getId(), message);
//            for (Long friendId : friendIds) {
//                objectDispatcher.sendObject(friendId, message);
//            }
//        }
//
//        private Friendship cloneFriendship(Friendship friendship) {
//            Friendship clone = new Friendship();
//            clone.setRequestingAccountId(friendship.getRequestingAccountId());
//            clone.setRespondingAccountId(friendship.getRespondingAccountId());
//            clone.setAccepted(friendship.getAccepted());
//            clone.setCreationTime(friendship.getCreationTime());
//            clone.setFriendAccount(friendship.getFriendAccount());
//            return clone;
//        }
//    }
}
