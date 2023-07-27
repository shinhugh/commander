package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class IdentificationManager implements IdentificationService {
    @Override
    public Account identifyAccount(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return (Account) principal;
    }

    @Override
    public boolean verifyAtLeastOneAuthority(Authentication authentication, Set<String> authorities) {
        if (authentication == null) {
            return false;
        }
        if (authorities == null || authorities.size() == 0) {
            return true;
        }
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(a -> authorities.contains(a.getAuthority()));
    }
}
