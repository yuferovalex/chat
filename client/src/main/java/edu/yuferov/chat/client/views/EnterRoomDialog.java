package edu.yuferov.chat.client.views;

import edu.yuferov.chat.client.controllers.EnterRoomDialogController;

import javax.swing.*;
import java.awt.event.*;

public class EnterRoomDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField;

    public EnterRoomDialog(EnterRoomDialogController controller, String roomName) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> controller.onOk(passwordField.getText()));

        buttonCancel.addActionListener(e -> controller.onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> controller.onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setTitle("Enter password for room " + roomName);
        pack();
    }
}
