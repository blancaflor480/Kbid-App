package com.example.myapplication.ui.user;

public class ModelUser {
    private String firstname;
    private String middlename;
    private String lastname;
    private String email;
    private String imageUrl;
    private String uid;
    private String onlineStatus;
    private String typingTo;
    private String birthday;
    private String gender;
    private String role;

    // No-argument constructor
    public ModelUser() {
        // Required for Firebase deserialization
    }

    // Constructor with arguments
    public ModelUser(String firstname, String middlename, String lastname, String email, String imageUrl, String uid, String onlineStatus, String typingTo, String birthday, String gender, String role) {
        this.firstname = firstname;
        this.middlename = middlename;
        this.lastname = lastname;
        this.email = email;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.birthday = birthday;
        this.gender = gender;
        this.role = role;
    }

    public ModelUser(String uid, String firstname, String middlename, String lastname, String birthday, String gender, String role, String email, String imageUrl) {
    }

    // Getters and setters...




    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Method to get the full name
    public String getName() {
        return firstname + " " + lastname;
    }
}
