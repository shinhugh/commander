package org.dev.commander.service.internal;

import org.dev.commander.model.Session;

public interface SessionEventHandler {
    void handleLogin(Session newSession);
    void handleLogout(Session deletedSession);
}
