package com.aps4sem.firebasechat;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String id;
    private String username;
    private String profileUrl;

    public User() { }

    public User(String id, String username) {
        this.id = id;
        this.username = username;
        this.profileUrl = "https://firebasestorage.googleapis.com/v0/b/fir-chat-7bc9b.appspot.com/o/profiles%2Fdefault_profile.png?alt=media&token=41a4c66b-273e-415e-bd1b-d1369adec53e";
    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        profileUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(profileUrl);
    }
}
