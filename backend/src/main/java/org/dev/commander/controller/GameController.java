package org.dev.commander.controller;

import org.dev.commander.model.GameEntry;
import org.dev.commander.service.GameService;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@Order(-1)
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameEntry> readGameEntries(Authentication authentication, @RequestParam(name = "accountId", required = false) Long accountId, @RequestParam(name = "id", required = false) Long id) {
        return gameService.readGameEntries(authentication, accountId, id);
    }

    @PostMapping
    public GameEntry createGame(Authentication authentication, @RequestBody GameEntry gameEntry) {
        return gameService.createGame(authentication, gameEntry);
    }

    @DeleteMapping
    public void leaveGame(Authentication authentication, @RequestParam("id") long id) {
        gameService.leaveGame(authentication, id);
    }
}
