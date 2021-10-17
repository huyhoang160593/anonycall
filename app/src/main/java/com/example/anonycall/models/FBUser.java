package com.example.anonycall.models;

public class FBUser {
    private String displayName;
    private String email;
    private String avatarURL;
    private String userID;

    public FBUser(){
    }
    public FBUser(String displayName, String email, String avatarURL, String userID) {
        this.displayName = displayName;
        this.email = email;
        this.avatarURL = avatarURL;
        this.userID = userID;
    }


    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "FBUser{" +
                "displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
