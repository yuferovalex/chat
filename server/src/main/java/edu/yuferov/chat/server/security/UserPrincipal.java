package edu.yuferov.chat.server.security;

import edu.yuferov.chat.server.domain.Session;
import edu.yuferov.chat.server.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPrincipal {
    @Getter
    private final Session session;
    private final Duration sessionLifetime;

    public UserPrincipal(Session session, Integer maxSessionInactivityDurationSec) {
        this.session = session;
        this.sessionLifetime = Duration.of(maxSessionInactivityDurationSec, ChronoUnit.SECONDS);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("USER"));
        boolean roomCreator = session.getUser().getId().equals(session.getUser().getRoom().getCreator().getId());
        if (roomCreator) {
            authorityList.add(new SimpleGrantedAuthority("ROOM_CREATOR"));
        }
        return authorityList;
    }

    public boolean isCredentialsNonExpired() {
        return session.isOpen() &&
                Duration.between(Instant.now(), session.getLastActivity())
                .compareTo(sessionLifetime) < 0;
    }

    public User getUser() {
        return session.getUser();
    }
}
