package edu.yuferov.chat.server.services;

import edu.yuferov.chat.server.security.SecurityContext;
import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.domain.Message;
import edu.yuferov.chat.server.domain.Room;
import edu.yuferov.chat.server.domain.User;
import edu.yuferov.chat.server.repositories.MessageRepository;
import edu.yuferov.chat.server.repositories.RoomRepository;
import edu.yuferov.chat.server.repositories.UserRepository;
import edu.yuferov.chat.server.services.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

public abstract class AbstractService {
    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected RoomRepository roomRepository;

    @Value("${server.server-user-name}")
    private String serverUserName;
    private static User serverUser;
    private static final Object serverUserLock = new Object();

    private void initServerUser() {
        if (serverUser == null) {
            synchronized (serverUserLock) {
                if (serverUser == null) {
                    serverUser = userRepository.findByName(serverUserName).get();
                }
            }
        }
    }

    protected User getServerUser() {
        initServerUser();
        return serverUser;
    }

    @Value("${server.default-room-name}")
    private String defaultRoomName;
    private static Room defaultRoom;

    private static final Object defaultRoomLock = new Object();

    private void initDefaultRoom() {
        if (defaultRoom == null) {
            synchronized (defaultRoomLock) {
                if (defaultRoom == null) {
                    defaultRoom = roomRepository.findByName(defaultRoomName).get();
                }
            }
        }
    }

    protected Room getDefaultRoom() {
        initDefaultRoom();
        return defaultRoom;
    }

    protected void notify(Room room, String msgBody) {
        messageRepository.save(new Message(Instant.now(), getServerUser(), room, msgBody));
    }

    protected void checkUserAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (!context.isAuthenticated()) {
            throw new ServiceException("you must login before perform this operation");
        }
    }

    protected void checkUserNotAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.isAuthenticated()) {
            throw new ServiceException("you cannot perform this operation because you already have sign up");
        }
    }
}
