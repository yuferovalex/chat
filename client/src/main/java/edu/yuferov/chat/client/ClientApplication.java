package edu.yuferov.chat.client;

import edu.yuferov.chat.client.controllers.LoginDialogController;
import edu.yuferov.chat.client.controllers.MainFormController;
import edu.yuferov.chat.client.io.Connection;

public class ClientApplication {
    public static void main(String[] args) {
        LoginDialogController loginDialog = new LoginDialogController();
        loginDialog.show();
        Connection connection = loginDialog.getConnection();
        if (connection == null) {
            System.exit(0);
        }
        MainFormController controller = new MainFormController(connection);
        controller.show();
    }
}
