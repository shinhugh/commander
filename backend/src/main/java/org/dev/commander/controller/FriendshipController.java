package org.dev.commander.controller;

import org.dev.commander.model.Friendship;
import org.dev.commander.service.FriendshipService;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendship")
@Order(-1)
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public List<Friendship> listFriendships(Authentication authentication) {
        return friendshipService.listFriendships(authentication);
    }

    @PostMapping
    public void requestFriendship(Authentication authentication, @RequestParam(name = "accountId", required = false) Long accountId) {
        friendshipService.requestFriendship(authentication, accountId);
    }

    @DeleteMapping
    public void terminateFriendship(Authentication authentication, @RequestParam(name = "accountId", required = false) Long accountId) {
        friendshipService.terminateFriendship(authentication, accountId);
    }
}
