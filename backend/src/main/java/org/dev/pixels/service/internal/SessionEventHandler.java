package org.dev.pixels.service.internal;

import org.dev.pixels.model.Session;

public interface SessionEventHandler {
    void handleLogin(Session newSession);
    void handleLogout(Session deletedSession);
}
