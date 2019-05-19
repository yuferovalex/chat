package edu.yuferov.chat.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    @ManyToOne(optional = false)
    private Room room;

    @OneToMany
    private List<Message> messages;

    public User(String name, String password, Room room) {
        this.name = name;
        this.password = password;
        this.room = room;
    }
}
