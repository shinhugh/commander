package org.dev.commander.service;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Account;
import org.dev.commander.model.Friendship;
import org.dev.commander.repository.FriendshipRepository;
import org.dev.commander.service.exception.ConflictException;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

@Service
public class FriendshipManager implements FriendshipService {
    private final Inner inner;

    public FriendshipManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public List<Friendship> listFriendships(Authentication authentication) throws NotAuthenticatedException {
        List<Friendship> friendships = inner.listFriendships(authentication);
        for (Iterator<Friendship> it = friendships.iterator(); it.hasNext();) {
            Friendship friendship = it.next();
            if (!friendship.getAccepted()) {
                it.remove();
                continue;
            }
            friendship.setAccepted(null);
            friendship.setRequestingAccountId(null);
            friendship.setRespondingAccountId(null);
        }
        return friendships;
    }

    @Override
    public void requestFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException, ConflictException {
        inner.requestFriendship(authentication, accountId);
    }

    @Override
    public void terminateFriendship(Authentication authentication, Long accountId) throws NotAuthenticatedException, IllegalArgumentException, NotFoundException {
        inner.terminateFriendship(authentication, accountId);
    }

    @Component
    @Transactional
    public static class Inner {
        private final FriendshipRepository friendshipRepository;
        private final AccountService accountService;
        private final AuthorizationService authorizationService;

        public Inner(FriendshipRepository friendshipRepository, AccountService accountService, AuthorizationService authorizationService) {
            this.friendshipRepository = friendshipRepository;
            this.accountService = accountService;
            this.authorizationService = authorizationService;
        }

        public List<Friendship> listFriendships(Authentication authentication) {
            Account clientAccount = authorizationService.getAccount(authentication);
            if (clientAccount == null) {
                throw new NotAuthenticatedException();
            }
            List<Friendship> friendships = friendshipRepository.findByAccountAIdOrAccountBId(clientAccount.getId(), clientAccount.getId());
            for (Iterator<Friendship> it = friendships.iterator(); it.hasNext();) {
                Friendship friendship = it.next();
                if (friendship.getAccepted()) {
                    Account requestingAccount, respondingAccount;
                    try {
                        requestingAccount = accountService.readAccount(authentication, friendship.getRequestingAccountId());
                        respondingAccount = accountService.readAccount(authentication, friendship.getRespondingAccountId());
                    }
                    catch (NotFoundException ex) {
                        it.remove();
                        // TODO: Do I need to call: friendshipRepository.delete(friendship);
                        continue;
                    }
                    friendship.setRequestingAccount(requestingAccount);
                    friendship.setRespondingAccount(respondingAccount);
                }
            }
            return friendships;
        }

        public void requestFriendship(Authentication authentication, Long accountId) {
            Account clientAccount = authorizationService.getAccount(authentication);
            if (clientAccount == null) {
                throw new NotAuthenticatedException();
            }
            if (accountId == null || accountId <= 0) {
                throw new IllegalArgumentException();
            }
            Friendship.Key key = new Friendship.Key();
            key.setAccountAId(clientAccount.getId());
            key.setAccountBId(accountId);
            Friendship friendship = friendshipRepository.findById(key).orElse(null);
            if (friendship == null) {
                key.setAccountAId(accountId);
                key.setAccountBId(clientAccount.getId());
                friendship = friendshipRepository.findById(key).orElse(null);
            }
            if (friendship == null) {
                accountService.readAccount(authentication, accountId);
                friendship = new Friendship();
                friendship.setRequestingAccountId(clientAccount.getId());
                friendship.setRespondingAccountId(accountId);
                friendship.setAccepted(false);
                friendship.setCreationTime(currentTimeMillis());
                friendshipRepository.save(friendship);
                // TODO: Notify via WebSocket
                return;
            }
            if (!friendship.getAccepted() && Objects.equals(friendship.getRespondingAccountId(), clientAccount.getId())) {
                friendship.setAccepted(true);
                return;
            }
            throw new ConflictException();
        }

        public void terminateFriendship(Authentication authentication, Long accountId) {
            Account clientAccount = authorizationService.getAccount(authentication);
            if (clientAccount == null) {
                throw new NotAuthenticatedException();
            }
            if (accountId == null || accountId <= 0) {
                throw new IllegalArgumentException();
            }
            // TODO: Implement
            throw new RuntimeException("Not implemented");
        }
    }
}
