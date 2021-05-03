package com.aps4sem.firebasechat;

public class User {

    private String id;
    private String username;
    private String profileUrl;

    public User() {
    }

    public User(String id, String username) {
        this.id = id;
        this.username = username;

        this.profileUrl = "https://firebasestorage.googleapis.com/v0/b/fir-chat-7bc9b.appspot.com/o/profiles%2Fdefault_profile.png?alt=media&token=41a4c66b-273e-415e-bd1b-d1369adec53e";
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
