package org.dev.pixels.service.internal;

import org.dev.pixels.model.Credentials;
import org.dev.pixels.model.Session;
import org.dev.pixels.service.exception.IllegalArgumentException;
import org.dev.pixels.service.exception.NotAuthenticatedException;

import java.util.List;

public interface SessionService {
    List<Session> readSessions(String token, Long accountId) throws IllegalArgumentException;
    Session login(Credentials credentials) throws IllegalArgumentException, NotAuthenticatedException;
    void logout(String token, Long accountId);
    void registerSessionEventHandler(SessionEventHandler sessionEventHandler);
}
