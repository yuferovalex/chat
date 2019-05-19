package edu.yuferov.chat.client.views;

import edu.yuferov.chat.client.controllers.LoginDialogController;

import javax.swing.*;
import java.awt.event.*;

public class LoginDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonLogin;
    private JButton buttonCancel;
    private JTextField serverAddress;
    private JTextField userLogin;
    private JPasswordField userPassword;
    private JButton buttonRegister;

    public LoginDialog(LoginDialogController controller) {
        getRootPane().setDefaultButton(buttonLogin);

        buttonLogin.addActionListener(e ->
                controller.onLogin(serverAddress.getText(), userLogin.getText(), userPassword.getText()));

        buttonRegister.addActionListener(e ->
                controller.onRegister(serverAddress.getText(), userLogin.getText(), userPassword.getText()));

        buttonCancel.addActionListener(e -> controller.onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setTitle("Login or Register");
        setContentPane(contentPane);
        setModal(true);
        pack();
        setLocationRelativeTo(null);
    }
}
