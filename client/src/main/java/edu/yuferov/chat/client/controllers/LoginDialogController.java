package edu.yuferov.chat.client.controllers;

import edu.yuferov.chat.client.io.Connection;
import edu.yuferov.chat.client.views.LoginDialog;
import edu.yuferov.chat.common.dto.responses.Response;
import lombok.Getter;

import javax.swing.*;

import java.util.Observable;
import java.util.Observer;

import static edu.yuferov.chat.common.Constants.DEFAULT_PORT;
import static edu.yuferov.chat.common.Utils.*;

public class LoginDialogController implements Observer {
    private LoginDialog dialog = new LoginDialog(this);

    @Getter
    private Connection connection;

    public void show() {
        dialog.setVisible(true);
    }

    public void onLogin(String address, String login, String password) {
        if (!validate(address, login, password)) {
            return;
        }
        createConnection(address);
        connection.login(login, password);
    }

    private void createConnection(String address) {
        connection = new Connection(address, DEFAULT_PORT);
        connection.addObserver(this);
        connection.start();
    }

    public void onRegister(String address, String login, String password) {
        if (!validate(address, login, password)) {
            return;
        }
        createConnection(address);
        connection.register(login, password);
    }

    public void onCancel() {
        if (connection != null) {
            connection.deleteObserver(this);
            connection = null;
        }
        dialog.dispose();
    }

    private void onSuccess() {
        connection.deleteObserver(this);
        dialog.dispose();
    }

    private void onError(String error) {
        if (connection != null) {
            connection.deleteObserver(this);
            connection = null;
        }
        JOptionPane.showMessageDialog(dialog, "Error occurred while signing up: " + error);
    }

    private boolean validate(String address, String login, String password) {
        try {
            validateServerAddress(address);
            validateUserName(login);
            validateUserPassword(password);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(dialog, "Error: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void update(Observable o, Object arg) {
        Response response = (Response) arg;
        if ("/user/login".equals(response.getPath()) || "/user/register".equals(response.getPath())) {
            if (!response.isError()) {
                SwingUtilities.invokeLater(this::onSuccess);
            } else {
                final String error = response.getError();
                SwingUtilities.invokeLater(() -> onError(error));
            }
        }
        if ("/connection".equals(response.getPath()) && response.isError()) {
            final String error = response.getError();
            SwingUtilities.invokeLater(() -> onError(error));
        }
    }
}
