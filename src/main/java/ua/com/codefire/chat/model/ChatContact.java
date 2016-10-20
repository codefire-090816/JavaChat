package ua.com.codefire.chat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by human on 10/20/16.
 */
public class ChatContact implements Serializable {

    private String address;
    private String nickname;
    private List<ChatMessage> chatMessages;
    private String tempMessage;

    public ChatContact(String address) {
        this(address, null);
    }

    public ChatContact(String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
        this.chatMessages = new ArrayList<>();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public String getTempMessage() {
        return tempMessage;
    }

    public void setTempMessage(String tempMessage) {
        this.tempMessage = tempMessage;
    }
}
