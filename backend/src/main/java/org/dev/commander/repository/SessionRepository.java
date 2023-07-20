package org.dev.commander.repository;

import org.dev.commander.model.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SessionRepository extends CrudRepository<Session, String> {
    List<Session> findByExpirationTimeLessThanEqual(long expirationTime);
    void deleteByAccountId(long accountId);
    void deleteByExpirationTimeLessThanEqual(long expirationTime);
}
