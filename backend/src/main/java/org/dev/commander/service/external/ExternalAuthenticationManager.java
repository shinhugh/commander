package org.dev.commander.service.external;

import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.internal.IdentificationService;
import org.dev.commander.service.internal.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalAuthenticationManager implements ExternalAuthenticationService {
    private final SessionService sessionService;
    private final IdentificationService identificationService;

    public ExternalAuthenticationManager(SessionService sessionService, IdentificationService identificationService) {
        this.sessionService = sessionService;
        this.identificationService = identificationService;
    }

    @Override
    public Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        Session session = null;
        if (authentication != null) {
            List<Session> sessions = sessionService.readSessions((String) authentication.getCredentials(), null);
            if (!sessions.isEmpty()) {
                session = sessions.get(0);
            }
        }
        if (session == null) {
            session = sessionService.login(credentials);
        }
        session.setAccountId(null);
        session.setAuthorities(null);
        return session;
    }

    @Override
    public void logout(Authentication authentication, boolean all) {
        if (authentication == null) {
            return;
        }
        String sessionToken = (String) authentication.getCredentials();
        long accountId = identificationService.identifyAccount(authentication).getId();
        sessionService.logout(all ? null : sessionToken, all ? accountId : null);
    }
}
