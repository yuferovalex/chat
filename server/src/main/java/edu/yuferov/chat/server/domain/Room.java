package edu.yuferov.chat.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(length = 72)
    private String password;

    @OneToMany(mappedBy = "room")
    private List<User> users;

    @OneToMany
    private List<Message> messages;

    @ManyToOne
    private User creator;

    public Room(String name, String password) {
        this.name = name;
        this.password = password;
    }

}
