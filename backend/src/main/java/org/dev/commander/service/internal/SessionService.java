package org.dev.commander.service.internal;

import org.dev.commander.model.Session;
import org.dev.commander.service.exception.IllegalArgumentException;
import org.dev.commander.service.exception.NotFoundException;

import java.util.List;

public interface SessionService {
    List<Session> readSessions(String token, Long accountId) throws IllegalArgumentException;
    Session createSession(Session session) throws IllegalArgumentException;
    Session updateSession(String token, Session session) throws IllegalArgumentException, NotFoundException;
    void deleteSession(String token) throws IllegalArgumentException, NotFoundException;
    void registerSessionEventHandler(SessionEventHandler sessionEventHandler);
}
