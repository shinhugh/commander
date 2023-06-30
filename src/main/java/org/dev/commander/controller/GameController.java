package org.dev.commander.controller;

import org.dev.commander.service.GameService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/test")
    public void test(Authentication authentication) {
        System.out.println("@@ GameController.test() invoked");
        gameService.test(authentication);
    }

    @PostMapping("/create")
    public void createGame(Authentication authentication) {
        System.out.println("@@ GameController.createGame() invoked");
        // TODO
    }

    @PostMapping("/leave")
    public void leaveGame(Authentication authentication) {
        System.out.println("@@ GameController.leaveGame() invoked");
        // TODO
    }

    @PostMapping("/move")
    public void makeMove(Authentication authentication) {
        System.out.println("@@ GameController.makeMove() invoked");
        // TODO
    }
}
