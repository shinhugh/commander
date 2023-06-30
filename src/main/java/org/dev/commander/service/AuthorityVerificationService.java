package org.dev.commander.service;

import org.springframework.security.core.Authentication;

import java.util.Set;

public interface AuthorityVerificationService {
    boolean verifyAuthenticationContainsAtLeastOneAuthority(Authentication authentication, Set<String> authorities);
}
