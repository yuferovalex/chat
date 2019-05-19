package edu.yuferov.chat.client.controllers;

import edu.yuferov.chat.client.io.Connection;
import edu.yuferov.chat.client.views.EnterRoomDialog;
import edu.yuferov.chat.common.dto.responses.Response;
import lombok.Getter;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class EnterRoomDialogController implements Observer {
    private EnterRoomDialog dialog;
    private Connection connection;
    private String roomName;

    public EnterRoomDialogController(Connection connection, String roomName) {
        this.connection = connection;
        this.roomName = roomName;
        this.dialog = new EnterRoomDialog(this, roomName);
    }

    public void show() {
        dialog.setVisible(true);
    }

    public void onCancel() {
        dialog.dispose();
    }

    public void onOk(String password) {
        if (password != null && password.isEmpty()) {
            password = null;
        }
        connection.enterRoom(roomName, password);
        connection.addObserver(this);
    }

    private void onSuccess() {
        connection.deleteObserver(this);
        dialog.dispose();
    }

    private void onError(String error) {
        connection.deleteObserver(this);
        JOptionPane.showMessageDialog(dialog, "Error occurred while enter room: " + error);
    }

    @Override
    public void update(Observable o, Object arg) {
        Response response = (Response) arg;
        if ("/room/enter".equals(response.getPath())) {
            if (!response.isError()) {
                SwingUtilities.invokeLater(this::onSuccess);
            } else {
                final String error = response.getError();
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
    }
}
