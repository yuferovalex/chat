package edu.yuferov.chat.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Lob
    private String body;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Room room;

    public Message(Instant timestamp, User user, Room room, String body) {
        this.timestamp = timestamp;
        this.room = room;
        this.user = user;
        this.body = body;
    }
}
