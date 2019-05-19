package edu.yuferov.chat.server.repositories;

import edu.yuferov.chat.server.domain.Room;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Long> {
    Optional<Room> findByName(String name);
    List<Room> findAll();
}
