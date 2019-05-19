package edu.yuferov.chat.server.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class Session {
    @Id
    @Getter
    @GeneratedValue
    private UUID id;

    @Getter
    @ManyToOne(optional = false)
    private User user;

    @Getter
    @Setter
    private Instant lastActivity;

    private boolean closed = false;

    public Session(@NotNull User user) {
        this.user = user;
    }

    public void close() {
        closed = true;
    }

    public boolean isOpen() {
        return !closed;
    }
}
