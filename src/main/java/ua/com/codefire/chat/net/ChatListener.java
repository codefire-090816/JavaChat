package ua.com.codefire.chat.net;

/**
 * Created by human on 10/13/16.
 */
public interface ChatListener {

    public void receiveMessage(String address, String message);
}
