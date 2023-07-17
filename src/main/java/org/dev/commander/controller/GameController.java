package org.dev.commander.controller;

import org.dev.commander.model.Game;
import org.dev.commander.service.GameService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<Long> readGameIds(Authentication authentication, @RequestParam("accountId") long accountId) {
        return gameService.readGameIds(authentication, accountId);
    }

    @GetMapping
    public Game readGame(Authentication authentication, @RequestParam("id") long id) {
        return gameService.readGame(authentication, id);
    }

    @PostMapping
    public Game createGame(Authentication authentication, @RequestBody Game game) {
        return gameService.createGame(authentication, game);
    }

    @DeleteMapping
    public void leaveGame(Authentication authentication, @RequestParam("id") long id) {
        gameService.leaveGame(authentication, id);
    }
}
