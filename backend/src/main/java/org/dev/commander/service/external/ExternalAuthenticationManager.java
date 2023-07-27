package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.service.internal.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalAuthenticationManager implements ExternalAuthenticationService {
    private final SessionService sessionService;

    public ExternalAuthenticationManager(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        if (authentication != null) {
            List<Session> sessions = sessionService.readSessions((String) authentication.getCredentials(), null);
            if (!sessions.isEmpty()) {
                return sessions.get(0);
            }
        }
        Session session = sessionService.login(credentials);
        session.setAccountId(null);
        session.setAuthorities(null);
        return session;
    }

    // TODO: Consider moving much of this logic into SessionManager
    @Override
    public void logout(Authentication authentication, Boolean all) {
        if (authentication == null) {
            return;
        }
        if (all != null && all) {
            long accountId = ((Account) authentication.getPrincipal()).getId();
            List<Session> sessions = sessionService.readSessions(null, accountId);
            for (Session session : sessions) {
                sessionService.logout(session.getToken());
            }
        } else {
            String sessionToken = (String) authentication.getCredentials();
            try {
                sessionService.logout(sessionToken);
            }
            catch (NotFoundException ignored) { }
        }
    }
}
