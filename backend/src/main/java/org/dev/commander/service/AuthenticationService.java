package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.Session;
import org.dev.commander.service.exception.NotFoundException;

public interface AuthenticationService {
    Session getSession(String token) throws NotFoundException;
    Account getSessionOwner(Session session) throws NotFoundException;
}
