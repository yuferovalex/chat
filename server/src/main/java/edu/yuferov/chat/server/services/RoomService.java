package edu.yuferov.chat.server.services;

import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.domain.Message;
import edu.yuferov.chat.server.domain.Room;
import edu.yuferov.chat.server.domain.User;
import edu.yuferov.chat.server.services.exceptions.ExistsAlreadyException;
import edu.yuferov.chat.server.services.exceptions.NotFoundException;
import edu.yuferov.chat.server.services.exceptions.ServiceException;
import edu.yuferov.chat.server.services.exceptions.WrongPasswordException;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RoomService extends AbstractService {
    public void create(String name, String password) throws ServiceException {
        checkUserAuthenticated();
        User user = SecurityContextHolder.getContext().getPrincipal().getUser();
        if (password != null) {
            if (password.isEmpty()) {
                password = null;
            } else {
                password = passwordEncoder.encode(password);
            }
        }
        Room newRoom = new Room(name, password);
        Room oldRoom = user.getRoom();
        newRoom.setCreator(user);
        try {
            roomRepository.save(newRoom);
        } catch (DataIntegrityViolationException e) {
            throw new ExistsAlreadyException("room with name " + name + " already exists");
        }
        user.setRoom(newRoom);
        userRepository.save(user);
        notifyAboutRoomChange(user, oldRoom, newRoom);
    }

    public void enter(String name, String password) throws ServiceException {
        checkUserAuthenticated();
        Room newRoom = roomRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("room " + name + " not found"));
        if (newRoom.getPassword() != null && (password == null ||
                !passwordEncoder.matches(password, newRoom.getPassword()))) {
            throw new WrongPasswordException("wrong password");
        }
        User user = SecurityContextHolder.getContext().getPrincipal().getUser();
        Room oldRoom = user.getRoom();
        user.setRoom(newRoom);
        userRepository.save(user);
        notifyAboutRoomChange(user, oldRoom, newRoom);
    }

    public void sendMessage(String body) {
        checkUserAuthenticated();
        User user = SecurityContextHolder.getContext().getPrincipal().getUser();
        Message message = new Message(Instant.now(), user, user.getRoom(), body);
        messageRepository.save(message);
    }

    public List<Message> getMessages(Instant from) {
        checkUserAuthenticated();
        User user = SecurityContextHolder.getContext().getPrincipal().getUser();
        Room room = user.getRoom();
        List<Message> result = messageRepository.findByRoomAndTimestampGreaterThan(room, from);
        Hibernate.initialize(result);
        return result;
    }

    public List<User> getUsers() {
        checkUserAuthenticated();
        User user = SecurityContextHolder.getContext().getPrincipal().getUser();
        Room room = user.getRoom();
        List<User> result = userRepository.findAllByRoom(room);
        Hibernate.initialize(result);
        return result;
    }

    public List<Room> getRoomList() {
        checkUserAuthenticated();
        return roomRepository.findAll();
    }

    private void notifyAboutRoomChange(User user, Room oldRoom, Room newRoom) {
        String userName = user.getName();
        String msgBodyForOldRoom = String.format("user %s left the room", userName);
        String msgBodyForNewRoom = String.format("user %s enter the room", userName);
        notify(oldRoom, msgBodyForOldRoom);
        notify(newRoom, msgBodyForNewRoom);
    }
}
