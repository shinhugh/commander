package org.dev.pixels.service.internal;

import org.dev.pixels.model.Account;
import org.springframework.security.core.Authentication;

import java.util.Set;

public interface IdentificationService {
    Account identifyAccount(Authentication authentication);
    boolean verifyAtLeastOneAuthority(Authentication authentication, Set<String> authorities);
}
