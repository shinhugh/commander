package org.dev.commander.service.internal;

import org.dev.commander.model.Account;
import org.springframework.security.core.Authentication;

import java.util.Set;

public interface IdentificationService {
    Account identifyAccount(Authentication authentication);
    boolean verifyAtLeastOneAuthority(Authentication authentication, Set<String> authorities);
}
