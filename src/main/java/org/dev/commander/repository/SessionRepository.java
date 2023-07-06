package org.dev.commander.repository;

import org.dev.commander.model.Session;
import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, String> {
    void deleteByAccountId(long accountId);
}
