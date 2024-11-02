package com.example.myapplication.database.userdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uid;
    private String childName;
    private String childAge;
    private String avatarName;
    private int avatarResourceId;
    private byte[] avatarImage;
    private String email;  // Field name is "email"

    // No-argument constructor required by Room
    public User() {
    }

    // Constructor including avatarImage
    public User(String childName, String childAge, String avatarName, int avatarResourceId, byte[] avatarImage, String email) {
        this.childName = childName;
        this.childAge = childAge;
        this.avatarName = avatarName;
        this.avatarResourceId = avatarResourceId;
        this.avatarImage = avatarImage;
        this.email = email;  // Set email field
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getChildAge() {
        return childAge;
    }

    public void setChildAge(String childAge) {
        this.childAge = childAge;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public int getAvatarResourceId() {
        return avatarResourceId;
    }

    public void setAvatarResourceId(int avatarResourceId) {
        this.avatarResourceId = avatarResourceId;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
    }

    public String getEmail() {  // Corrected getter name
        return email;
    }

    public void setEmail(String email) {  // Corrected setter name
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
