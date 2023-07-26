package org.dev.commander.service.internal;

import org.dev.commander.model.Session;

public interface SessionEventHandler {
    void handleCreateSession(Session newSession);
    void handleUpdateSession(Session preUpdateSession, Session postUpdateSession);
    void handleDeleteSession(Session deletedSession);
}
