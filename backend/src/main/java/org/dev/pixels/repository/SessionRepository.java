package org.dev.pixels.repository;

import org.dev.pixels.model.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SessionRepository extends CrudRepository<Session, String> {
    List<Session> findByAccountId(Long accountId);
    List<Session> findByExpirationTimeLessThanEqual(Long expirationTime);
    void deleteByAccountId(Long accountId);
    void deleteByExpirationTimeLessThanEqual(Long expirationTime);
}
