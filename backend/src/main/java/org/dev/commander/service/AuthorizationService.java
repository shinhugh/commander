package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;

import java.util.Set;

public interface AuthorizationService {
    Account getAccount(Authentication authentication);
    boolean verifyAuthenticationContainsAtLeastOneAuthority(Authentication authentication, Set<String> authorities);
}
