package edu.yuferov.chat.client.controllers;

import edu.yuferov.chat.client.io.Connection;
import edu.yuferov.chat.client.views.MainForm;
import edu.yuferov.chat.common.dto.MessageDto;
import edu.yuferov.chat.common.dto.RoomDto;
import edu.yuferov.chat.common.dto.UserDto;
import edu.yuferov.chat.common.dto.responses.MessageListResponseBody;
import edu.yuferov.chat.common.dto.responses.Response;
import edu.yuferov.chat.common.dto.responses.RoomListResponseBody;
import edu.yuferov.chat.common.dto.responses.UserListResponseBody;

import javax.swing.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainFormController implements Observer {
    private MainForm mainForm;
    private Connection connection;
    private Timer updateDataTimer;

    public MainFormController(Connection connection) {
        this.connection = connection;
        this.mainForm = new MainForm(this);

        connection.addObserver(this);
        updateDataTimer = new Timer(1000, (e) -> updateData());
        updateDataTimer.start();
    }

    public void show() {
        mainForm.setVisible(true);
    }

    public void onCreateRoom() {
        CreateRoomDialogController dialog = new CreateRoomDialogController(connection);
        dialog.show();
    }

    public void onSendMessage(String message) {
        connection.sendMessage(message);
    }

    public void onChangeRoom(String roomName) {
        EnterRoomDialogController dialog = new EnterRoomDialogController(connection, roomName);
        dialog.show();
    }

    private void updateData() {
        connection.getRoomMessages();
        connection.getRoomUsers();
        connection.getRoomList();
    }

    private void appendMessages(List<MessageDto> messages) {
        mainForm.appendMessages(messages);
    }

    private void setRoomList(List<RoomDto> rooms) {
        mainForm.setRoomList(rooms);
    }

    private void setUserList(List<UserDto> users) {
        mainForm.setUserList(users);
    }

    private void onError(String error) {
        JOptionPane.showMessageDialog(mainForm, "Error: " + error);
    }

    private void onConnectionError(String error) {
        updateDataTimer.stop();
        JOptionPane.showMessageDialog(mainForm, "Connection error: " + error);
        System.exit(1);
    }

    @Override
    public void update(Observable connection, Object arg) {
        Response response = (Response) arg;
        String error = response.getError();
        if ("/room/getMessages".equals(response.getPath())) {
            if (!response.isError()) {
                MessageListResponseBody body = (MessageListResponseBody) response.getData();
                List<MessageDto> messages = body.getMessages();
                SwingUtilities.invokeLater(() -> appendMessages(messages));
            } else {
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
        if ("/room/list".equals(response.getPath())) {
            if (!response.isError()) {
                RoomListResponseBody body = (RoomListResponseBody) response.getData();
                List<RoomDto> rooms = body.getRooms();
                SwingUtilities.invokeLater(() -> setRoomList(rooms));
            } else {
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
        if ("/room/getUsers".equals(response.getPath())) {
            if (!response.isError()) {
                UserListResponseBody body = (UserListResponseBody) response.getData();
                List<UserDto> users = body.getUsers();
                SwingUtilities.invokeLater(() -> setUserList(users));
            } else {
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
        if ("/connection".equals(response.getPath()) && response.isError()) {
            SwingUtilities.invokeLater(() -> onConnectionError(error));
        }
    }


}
