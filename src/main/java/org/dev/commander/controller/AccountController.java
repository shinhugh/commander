package org.dev.commander.controller;

import org.dev.commander.model.Account;
import org.dev.commander.service.AccountService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Account readAccount(Authentication authentication, @RequestParam("id") long id) {
        return accountService.readAccountById(authentication, id);
    }

    @PostMapping
    public Account createAccount(Authentication authentication, @RequestBody Account account) {
        return accountService.createAccount(authentication, account);
    }

    @PutMapping
    public Account updateAccount(Authentication authentication, @RequestParam("id") long id, @RequestBody Account account) {
        return accountService.updateAccountById(authentication, id, account);
    }

    @DeleteMapping
    public void deleteAccount(Authentication authentication, @RequestParam("id") long id) {
        this.accountService.deleteAccountById(authentication, id);
    }
}
