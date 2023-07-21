package org.dev.commander.controller;

import org.dev.commander.model.Account;
import org.dev.commander.service.AccountService;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@Order(-1)
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Account readAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id) {
        return accountService.readAccount(authentication, id);
    }

    @PostMapping
    public Account createAccount(Authentication authentication, @RequestBody(required = false) Account account) {
        return accountService.createAccount(authentication, account);
    }

    @PutMapping
    public Account updateAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id, @RequestBody(required = false) Account account) {
        return accountService.updateAccount(authentication, id, account);
    }

    @DeleteMapping
    public void deleteAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id) {
        accountService.deleteAccount(authentication, id);
    }
}
