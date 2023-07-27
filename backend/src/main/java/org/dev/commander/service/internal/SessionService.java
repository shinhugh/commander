package org.dev.commander.service.internal;

import org.dev.commander.model.Credentials;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotAuthenticatedException;

import java.util.List;

public interface SessionService {
    List<Session> readSessions(String token, Long accountId) throws IllegalArgumentException;
    Session login(Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException;
    void logout(String token, Long accountId);
    void registerSessionEventHandler(SessionEventHandler sessionEventHandler);
}
