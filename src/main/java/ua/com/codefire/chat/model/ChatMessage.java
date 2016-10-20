package ua.com.codefire.chat.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by human on 10/20/16.
 */
public class ChatMessage implements Serializable {

    private Date timestamp;
    private String message;
    private String address;
    private boolean income;
    private boolean read;

    public ChatMessage(Date timestamp, String message, String address, boolean income, boolean read) {
        this.timestamp = timestamp;
        this.message = message;
        this.address = address;
        this.income = income;
        this.read = read;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isIncome() {
        return income;
    }

    public void setIncome(boolean income) {
        this.income = income;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", address='" + address + '\'' +
                ", income=" + income +
                ", read=" + read +
                '}';
    }
}
