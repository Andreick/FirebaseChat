package com.aps4sem.firebasechat;

public class Message {

    private String text;
    private String senderUid;
    private String receiverUid;
    private long timestamp;

    public Message() { }

    public Message(String text, String senderUid, String receiverUid) {
        this.text = text;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
