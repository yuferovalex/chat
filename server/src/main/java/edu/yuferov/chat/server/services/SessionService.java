package edu.yuferov.chat.server.services;

import edu.yuferov.chat.server.security.SecurityContext;
import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.security.UserPrincipal;
import edu.yuferov.chat.server.domain.Session;
import edu.yuferov.chat.server.domain.User;
import edu.yuferov.chat.server.repositories.SessionRepository;
import edu.yuferov.chat.server.services.exceptions.CredentialsExpiredException;
import edu.yuferov.chat.server.services.exceptions.NotFoundException;
import edu.yuferov.chat.server.services.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionService extends AbstractService {
    @Autowired
    private SessionRepository sessionRepository;

    @Value("${server.security.max-session-inactivity-duration-sec}")
    private Integer maxSessionInactivityDurationSec;

    public UserPrincipal loadUserBySessionId(String sessionId) throws ServiceException {
        UserPrincipal principal = new UserPrincipal(sessionRepository.findById(UUID.fromString(sessionId))
               .orElseThrow(() -> new NotFoundException("session not found")),
               maxSessionInactivityDurationSec);
        if (principal.isCredentialsNonExpired()) {
            return principal;
        }
        throw new CredentialsExpiredException("your credentials have been expired");
    }

    public void create(User user) {
        Session session = new Session(user);
        sessionRepository.save(session);
        UserPrincipal principal = new UserPrincipal(session, maxSessionInactivityDurationSec);
        SecurityContextHolder.getContext().setPrincipal(principal);
    }

    public void save() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.isAuthenticated()) {
            Session session = context.getPrincipal().getSession();
            session.setLastActivity(Instant.now());
            sessionRepository.save(session);
        }
    }

    public void exit() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.isAuthenticated()) {
            Session session = context.getPrincipal().getSession();
            session.close();
            sessionRepository.save(session);
        }
    }

    @Scheduled(fixedDelay = 10)
    public void closeAbandonedSessions() {
        Instant now = Instant.now();
        Duration maxInactivity = Duration.of(maxSessionInactivityDurationSec, ChronoUnit.SECONDS);
        sessionRepository.findAllByClosedIsFalse().forEach(session -> {
            Duration inactivityDuration = Duration.between(session.getLastActivity(), now);
            if (inactivityDuration.compareTo(maxInactivity) > 0) {
                User user = session.getUser();
                String msg = String.format("user %s disconnected", user.getName());
                notify(user.getRoom(), msg);
                session.close();
                sessionRepository.save(session);
            }
        });
    }
}
