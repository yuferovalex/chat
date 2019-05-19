package edu.yuferov.chat.client.controllers;

import edu.yuferov.chat.client.io.Connection;
import edu.yuferov.chat.client.views.CreateRoomDialog;
import edu.yuferov.chat.common.dto.responses.Response;
import lombok.Getter;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class CreateRoomDialogController implements Observer {
    private CreateRoomDialog dialog = new CreateRoomDialog(this);
    private Connection connection;
    @Getter
    private boolean created;

    public CreateRoomDialogController(Connection connection) {
        this.connection = connection;
    }

    public void show() {
        dialog.setVisible(true);
        created = false;
    }

    public void onOK(String name, String password) {
        if (!validate(name, password)) {
            return;
        }
        connection.addObserver(this);
        connection.createRoom(name, password);
    }

    public void onCancel() {
        connection.deleteObserver(this);
        dialog.dispose();
    }

    private void onSuccess() {
        connection.deleteObserver(this);
        dialog.dispose();
    }

    private void onError(String error) {
        connection.deleteObserver(this);
        JOptionPane.showMessageDialog(dialog, "Error occurred while creating room: " + error);
    }

    private boolean validate(String name, String password) {
        return true;
    }

    @Override
    public void update(Observable observable, Object arg) {
        Response response = (Response) arg;
        if ("/room/create".equals(response.getPath())) {
            if (!response.isError()) {
                SwingUtilities.invokeLater(this::onSuccess);
            } else {
                final String error = response.getError();
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
    }
}
