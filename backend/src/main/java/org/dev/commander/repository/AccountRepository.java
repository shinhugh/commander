package org.dev.commander.repository;

import org.dev.commander.model.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    boolean existsByLoginNameIgnoreCase(String loginName);
    Optional<Account> findByLoginName(String loginName);
    List<Account> findByLoginNameIgnoreCase(String loginName);
}
