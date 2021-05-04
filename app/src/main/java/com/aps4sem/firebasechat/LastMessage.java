package com.aps4sem.firebasechat;

public class LastMessage {

    private User contact;
    private String senderUid;
    private String text;
    private long timestamp;

    public LastMessage() { }

    public LastMessage(User contact, String senderUid, String text, long timestamp) {
        this.contact = contact;
        this.senderUid = senderUid;
        this.text = text;
        this.timestamp = timestamp;
    }

    public User getContact() {
        return contact;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
