package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.Session;

public interface AuthenticationService {
    Session getSession(String token) throws NotFoundException;
    Account getSessionOwner(Session session) throws NotFoundException;
}
