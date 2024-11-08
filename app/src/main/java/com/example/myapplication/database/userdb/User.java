package com.example.myapplication.database.userdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uid;
    private String childName;
    private String childBirthday; // Updated from childAge to childBirthday
    private String avatarName;
    private int avatarResourceId;
    private byte[] avatarImage;
    private String email;  // Field name is "email"

    // No-argument constructor required by Room
    public User() {
    }

    // Constructor including avatarImage and updated field name
    public User(String childName, String childBirthday, String avatarName, int avatarResourceId, byte[] avatarImage, String email) {
        this.childName = childName;
        this.childBirthday = childBirthday; // Set childBirthday field
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

    public String getChildBirthday() { // Updated getter name
        return childBirthday;
    }

    public void setChildBirthday(String childBirthday) { // Updated setter name
        this.childBirthday = childBirthday;
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
