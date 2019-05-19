package edu.yuferov.chat.server.repositories;

import edu.yuferov.chat.server.domain.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SessionRepository extends CrudRepository<Session, UUID> {
    Iterable<Session> findAllByClosedIsFalse();
}
