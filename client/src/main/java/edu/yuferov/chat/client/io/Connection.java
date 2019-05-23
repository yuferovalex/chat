package edu.yuferov.chat.client.io;

import edu.yuferov.chat.common.dto.MessageDto;
import edu.yuferov.chat.common.dto.requests.GetMessageRequestParams;
import edu.yuferov.chat.common.dto.requests.NamePasswordRequestParams;
import edu.yuferov.chat.common.dto.requests.Request;
import edu.yuferov.chat.common.dto.requests.SendMessageRequestParams;
import edu.yuferov.chat.common.dto.responses.MessageListResponseBody;
import edu.yuferov.chat.common.dto.responses.Response;
import edu.yuferov.chat.common.dto.responses.SessionIdResponseBody;

import java.time.Instant;
import java.util.List;
import java.util.Observable;

public class Connection extends Observable {
    private ConnectionWorker worker;
    private Thread thread;
    private String session;
    private Instant lastMessagesUpdateTime;

    public Connection(String address, int port) {
        this.worker = new ConnectionWorker(this, address, port);
        this.thread = new Thread(this.worker);
    }

    public void start() {
        thread.start();
    }

    public void login(String username, String password) {
        worker.addRequest(Request.builder()
                .path("/user/login")
                .params(NamePasswordRequestParams.builder()
                        .name(username)
                        .password(password)
                        .build())
                .build());
    }

    public void register(String username, String password) {
        worker.addRequest(Request.builder()
                .path("/user/register")
                .params(NamePasswordRequestParams.builder()
                        .name(username)
                        .password(password)
                        .build())
                .build());
    }

    public void sendMessage(String message) {
        checkSessionNotNull();
        worker.addRequest(Request.builder()
                .path("/room/sendMessage")
                .session(session)
                .params(new SendMessageRequestParams(message))
                .build());
    }

    public void createRoom(String name, String password) {
        checkSessionNotNull();
        worker.addRequest(Request.builder()
                .path("/room/create")
                .session(session)
                .params(NamePasswordRequestParams.builder()
                        .name(name)
                        .password(password)
                        .build())
                .build());
    }

    public void enterRoom(String name, String password) {
        checkSessionNotNull();
        worker.addRequest(Request.builder()
                .path("/room/enter")
                .session(session)
                .params(NamePasswordRequestParams.builder()
                        .name(name)
                        .password(password)
                        .build())
                .build());
    }

    public void getRoomList() {
        checkSessionNotNull();
        worker.addRequest(Request.builder()
                .path("/room/list")
                .session(session)
                .build());
    }

    public void getRoomMessages() {
        checkSessionNotNull();
        if (lastMessagesUpdateTime == null) {
            lastMessagesUpdateTime = Instant.now().minusSeconds(120);
        }
        worker.addRequest(Request.builder()
                .path("/room/getMessages")
                .session(session)
                .params(new GetMessageRequestParams(lastMessagesUpdateTime.toString()))
                .build());
    }

    public void getRoomUsers() {
        checkSessionNotNull();
        worker.addRequest(Request.builder()
                .path("/room/getUsers")
                .session(session)
                .build());
    }

    void handleResponse(Response response) {
        if (!response.isError() && ("/user/register".equals(response.getPath())
                || "/user/login".equals(response.getPath()))) {
            SessionIdResponseBody body = (SessionIdResponseBody) response.getData();
            session = body.getSessionId();
        }
        if (!response.isError() && "/room/getMessages".equals(response.getPath())) {
            MessageListResponseBody body = (MessageListResponseBody) response.getData();
            updateLastMessagesUpdateTime(body.getMessages());
        }
        setChanged();
        notifyObservers(response);
    }

    private void updateLastMessagesUpdateTime(List<MessageDto> messages) {
        if (messages.size() > 0) {
            lastMessagesUpdateTime = Instant.parse(messages.get(messages.size() - 1).getTime());
        }
    }

    void connected() {
        setChanged();
        notifyObservers(Response.builder()
                .path("/connection")
                .status(200)
                .build());
    }

    void connectionError(String message) {
        setChanged();
        notifyObservers(Response.builder()
                .path("/connection")
                .status(400)
                .error(message)
                .build());
    }

    private void checkSessionNotNull() {
        if (session == null) {
            throw new Error("session is null");
        }
    }

    public void disconnected() {
        setChanged();
        notifyObservers(Response.builder()
                .path("/connection")
                .status(500)
                .error("server disconnected")
                .build());
    }
}
