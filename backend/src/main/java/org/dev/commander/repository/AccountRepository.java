package org.dev.commander.repository;

import org.dev.commander.model.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByLoginName(String loginName);
    boolean existsByLoginNameIgnoreCase(String loginName);
}
