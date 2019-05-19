package edu.yuferov.chat.server.repositories;

import edu.yuferov.chat.server.domain.Message;
import edu.yuferov.chat.server.domain.Room;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findByRoomAndTimestampGreaterThan(Room room, Instant from);
}
