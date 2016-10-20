package ua.com.codefire.chat.ui;

/**
 * Created by human on 10/20/16.
 */
public class ContactModel {

    private String address;
    private String nickname;

    public ContactModel(String address) {
        this.address = address;
    }

    public ContactModel(String address, String nickname) {
        this.address = address;
        this.nickname = nickname;
    }

    public String getAddress() {
        return address;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return String.format("(%s: %s)", address, nickname);
    }

}
