package edu.yuferov.chat.server.controllers;

import edu.yuferov.chat.common.dto.MessageDto;
import edu.yuferov.chat.common.dto.RoomDto;
import edu.yuferov.chat.common.dto.UserDto;
import edu.yuferov.chat.common.dto.requests.GetMessageRequestParams;
import edu.yuferov.chat.common.dto.requests.NamePasswordRequestParams;
import edu.yuferov.chat.common.dto.requests.SendMessageRequestParams;
import edu.yuferov.chat.common.dto.responses.MessageListResponseBody;
import edu.yuferov.chat.common.dto.responses.RoomListResponseBody;
import edu.yuferov.chat.common.dto.responses.UserListResponseBody;
import edu.yuferov.chat.server.controllers.meta.RequestBody;
import edu.yuferov.chat.server.controllers.meta.RequestMapping;
import edu.yuferov.chat.server.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/room")
public class RoomController {
    @Autowired
    private RoomService service;

    @RequestMapping("/create")
    public void create(@RequestBody NamePasswordRequestParams params) {
        if (params == null || params.getName() == null) {
            throw new IllegalArgumentException("parameters name can not be null");
        }
        service.create(params.getName(), params.getPassword());
    }

    @RequestMapping("/enter")
    public void enter(@RequestBody NamePasswordRequestParams params) {
        if (params == null || params.getName() == null) {
            throw new IllegalArgumentException("parameter name can not be null");
        }
        service.enter(params.getName(), params.getPassword());
    }

    @RequestMapping("/list")
    public RoomListResponseBody list() {
        return new RoomListResponseBody(
                service.getRoomList().stream()
                        .map(room -> new RoomDto(room.getName()))
                        .collect(Collectors.toList()));
    }

    @RequestMapping("/sendMessage")
    public void sendMessage(@RequestBody SendMessageRequestParams params) {
        if (params == null || params.getMessage() == null) {
            throw new IllegalArgumentException("parameter message can not be null");
        }
        service.sendMessage(params.getMessage());
    }

    @RequestMapping("/getMessages")
    public MessageListResponseBody getMessages(@RequestBody GetMessageRequestParams params) {
        if (params == null || params.getFrom() == null) {
            throw new IllegalArgumentException("parameter from can not be null");
        }
        Instant from;
        try {
            from = Instant.parse(params.getFrom());
        } catch (Exception e) {
            throw new IllegalArgumentException("wrong date format");
        }
        return new MessageListResponseBody(
                service.getMessages(from).stream()
                        .map(msg -> new MessageDto(msg.getTimestamp().toString(),
                                msg.getUser().getName(), msg.getBody()))
                        .collect(Collectors.toList()));
    }

    @RequestMapping("/getUsers")
    public UserListResponseBody getUsers() {
        return new UserListResponseBody(
                service.getUsers().stream()
                        .map(user -> new UserDto(user.getName()))
                        .collect(Collectors.toList()));
    }
}
