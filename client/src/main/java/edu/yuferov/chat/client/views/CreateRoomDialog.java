package edu.yuferov.chat.client.views;

import edu.yuferov.chat.client.controllers.CreateRoomDialogController;

import javax.swing.*;
import java.awt.event.*;

public class CreateRoomDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JPasswordField passwordField;

    public CreateRoomDialog(CreateRoomDialogController controller) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> controller.onOK(nameField.getText(), passwordField.getText()));
        buttonCancel.addActionListener(e -> controller.onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                e -> controller.onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setTitle("Create room");
        pack();
    }
}
