package ua.com.codefire.chat.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by human on 10/11/16.
 */
public class Server implements Runnable {

    private static final int SERVER_PORT = 21347;

    private List<ChatListener> listenerList;

    private boolean listen;

    public Server() {
        this.listenerList = Collections.synchronizedList(new ArrayList<ChatListener>());
    }

    public boolean add(ChatListener chatListener) {
        return listenerList.add(chatListener);
    }

    public boolean remove(ChatListener chatListener) {
        return listenerList.remove(chatListener);
    }

    @Override
    public void run() {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(SERVER_PORT);
            ss.setSoTimeout(1000);
            System.out.println("SERVER PORT REGISTERED");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ss != null) {
            System.out.println("SERVER PORT LISTEN");
            listen = true;

            // Start full-time server
            while (listen) {
                // Waiting for [1] client
                try {
                    Socket accepted = ss.accept();
                    accepted.setSoTimeout(10000);

                    threadPool.execute(new ClientWorker(this, accepted));
                } catch (SocketTimeoutException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        threadPool.shutdown();
    }

    public void triggerReceiveMessage(String address, String message) {
        for (ChatListener chatListener : listenerList) {
            chatListener.receiveMessage(address, message);
        }
    }

}
