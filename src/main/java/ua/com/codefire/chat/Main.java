package ua.com.codefire.chat;

import ua.com.codefire.chat.ui.MainFrame;
import ua.com.codefire.chat.net.Server;

import java.awt.*;

/**
 * Created by human on 10/11/16.
 */
public class Main {

    private static final int SERVER_PORT = 21347;

    public static void main(String[] args) {
        final Server server = new Server();
        new Thread(server).start();

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame("Java CHAT");

                server.add(mainFrame);
                mainFrame.setVisible(true);
            }
        });

    }
}
