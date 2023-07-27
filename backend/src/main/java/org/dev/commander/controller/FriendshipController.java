package org.dev.commander.controller;

import org.dev.commander.model.Friendships;
import org.dev.commander.service.external.ExternalFriendshipService;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friendship")
@Order(-1)
public class FriendshipController {
    private final ExternalFriendshipService externalFriendshipService;

    public FriendshipController(ExternalFriendshipService externalFriendshipService) {
        this.externalFriendshipService = externalFriendshipService;
    }

    @GetMapping
    public Friendships listFriendships(Authentication authentication) {
        return externalFriendshipService.listFriendships(authentication);
    }

    @PostMapping
    public void requestFriendship(Authentication authentication, @RequestParam(name = "id", required = false) Long accountId) {
        externalFriendshipService.requestFriendship(authentication, accountId);
    }

    @DeleteMapping
    public void terminateFriendship(Authentication authentication, @RequestParam(name = "id", required = false) Long accountId) {
        externalFriendshipService.terminateFriendship(authentication, accountId);
    }
}
