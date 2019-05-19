package edu.yuferov.chat.server.repositories;

import edu.yuferov.chat.server.domain.Room;
import edu.yuferov.chat.server.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
    List<User> findAllByRoom(Room room);
}
