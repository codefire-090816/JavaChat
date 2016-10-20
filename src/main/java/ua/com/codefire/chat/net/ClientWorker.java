package ua.com.codefire.chat.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by human on 10/11/16.
 */
public class ClientWorker implements Runnable {

    private Server server;
    private Socket accepted;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientWorker(Server server, Socket accepted) throws IOException {
        this.server = server;
        this.accepted = accepted;
        // Wrapping client I/O
        this.dis = new DataInputStream(accepted.getInputStream());
        this.dos = new DataOutputStream(accepted.getOutputStream());
    }

    @Override
    public void run() {
        // Get connected client IP address
        System.out.println("CONNECTED: " + accepted.getInetAddress().getHostAddress());

        try {
            // Get message from client
            String message = dis.readUTF();

            // Send answer to client
            dos.writeUTF("OK");
            dos.flush();


            server.triggerReceiveMessage(accepted.getInetAddress().getHostAddress(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
