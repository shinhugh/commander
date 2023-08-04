package org.dev.pixels.controller;

import org.dev.pixels.model.Account;
import org.dev.pixels.service.external.ExternalAccountService;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@Order(-1)
public class AccountController {
    private final ExternalAccountService externalAccountService;

    public AccountController(ExternalAccountService externalAccountService) {
        this.externalAccountService = externalAccountService;
    }

    @GetMapping
    public List<Account> readAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id) {
        return externalAccountService.readAccounts(authentication, id);
    }

    @PostMapping
    public Account createAccount(Authentication authentication, @RequestBody(required = false) Account account) {
        return externalAccountService.createAccount(authentication, account);
    }

    @PutMapping
    public Account updateAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id, @RequestBody(required = false) Account account) {
        return externalAccountService.updateAccount(authentication, id, account);
    }

    @DeleteMapping
    public void deleteAccount(Authentication authentication, @RequestParam(name = "id", required = false) Long id) {
        externalAccountService.deleteAccount(authentication, id);
    }
}
