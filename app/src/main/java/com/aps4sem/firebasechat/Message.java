package com.aps4sem.firebasechat;

public class Message {

    private String text;
    private String fromUid;
    private String toUid;
    private long timestamp;

    public Message() { }

    public Message(String text, String fromUid, String toUid) {
        this.text = text;
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public String getFromUid() {
        return fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
