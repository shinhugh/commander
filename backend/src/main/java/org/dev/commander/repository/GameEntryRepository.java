package org.dev.commander.repository;

import org.dev.commander.model.GameEntry;
import org.springframework.data.repository.CrudRepository;

public interface GameEntryRepository extends CrudRepository<GameEntry, Long> { }
