package edu.yuferov.chat.client.views;

import edu.yuferov.chat.client.controllers.MainFormController;
import edu.yuferov.chat.common.dto.MessageDto;
import edu.yuferov.chat.common.dto.RoomDto;
import edu.yuferov.chat.common.dto.UserDto;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainForm extends JFrame {
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JList roomList;
    private JTextArea userList;
    private JButton createRoomButton;
    private JTextArea messageList;
    private JTextField messageField;
    private JButton sendMessageButton;
    private DefaultListModel defaultListModel = new DefaultListModel();

    public MainForm(MainFormController controller) {
        createRoomButton.addActionListener(e -> controller.onCreateRoom());
        sendMessageButton.addActionListener(e -> {
            controller.onSendMessage(messageField.getText());
            messageField.setText("");
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        roomList.setModel(defaultListModel);
        setTitle("Chat client");
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    controller.onChangeRoom((String) roomList.getSelectedValue());
                }
                super.mouseClicked(e);
            }
        });
    }

    public void appendMessages(List<MessageDto> messages) {
        if (messages.size() == 0) {
            return;
        }
        String text = messageList.getText().trim();
        Stream<String> stream = Stream.of();
        if (!StringUtils.isBlank(text)) {
            stream = Stream.concat(stream, Stream.of(text));
        }
        stream = Stream.concat(stream, messages.stream().map(this::formatMessage));
        String messageText = stream.collect(Collectors.joining("\n"));
        messageList.setText(messageText);
    }

    public void setRoomList(List<RoomDto> rooms) {
        defaultListModel.clear();
        for (int i = 0; i < rooms.size(); ++i) {
            defaultListModel.add(i, rooms.get(i).getName());
        }
    }

    public void setUserList(List<UserDto> users) {
        userList.setText(users.stream()
                .map(UserDto::getName)
                .collect(Collectors.joining("\n")));
    }

    private String formatMessage(MessageDto message) {
        return String.format("[%s] %s: %s%n",
                message.getTime(),
                message.getAuthor(),
                message.getBody());
    }
}
