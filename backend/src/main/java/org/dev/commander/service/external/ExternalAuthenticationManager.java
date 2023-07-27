package org.dev.commander.service.external;

import org.dev.commander.model.Account;
import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;
import org.dev.commander.service.exception.NotFoundException;
import org.dev.commander.service.internal.AccountService;
import org.dev.commander.service.internal.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalAuthenticationManager implements ExternalAuthenticationService {
    private final SessionService sessionService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public ExternalAuthenticationManager(SessionService sessionService, AccountService accountService, PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    // TODO: Consider moving much of this logic into SessionManager
    @Override
    public Session login(Authentication authentication, Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException {
        if (authentication != null) {
            List<Session> sessions = sessionService.readSessions((String) authentication.getCredentials(), null);
            if (!sessions.isEmpty()) {
                return sessions.get(0);
            }
        }
        if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
            throw new IllegalArgumentException();
        }
        List<Account> accounts = accountService.readAccounts(null, credentials.getUsername());
        if (accounts.isEmpty()) {
            throw new NotAuthenticatedException();
        }
        Account account = accounts.get(0);
        if (!passwordEncoder.matches(credentials.getPassword(), account.getPassword())) {
            throw new NotAuthenticatedException();
        }
        Session session = new Session();
        session.setAccountId(account.getId());
        session.setAuthorities(account.getAuthorities());
        session = sessionService.createSession(session);
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
                sessionService.deleteSession(session.getToken());
            }
        } else {
            String sessionToken = (String) authentication.getCredentials();
            try {
                sessionService.deleteSession(sessionToken);
            }
            catch (NotFoundException ignored) { }
        }
    }
}
