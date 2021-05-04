package com.aps4sem.firebasechat;

public class LastMessage {

    private User user;
    private Message message;

    public LastMessage() { }

    public LastMessage(User user, Message message) {
        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public Message getMessage() {
        return message;
    }
}
