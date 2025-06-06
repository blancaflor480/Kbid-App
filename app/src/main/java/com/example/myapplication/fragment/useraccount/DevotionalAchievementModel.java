package com.example.myapplication.fragment.useraccount;

public class DevotionalAchievementModel {
    private String id;
    private int badge;
    private String badgename;
    private String email;
    private String controlId;

    // Constructor for creating badge models with drawable resource
    public DevotionalAchievementModel(int badge) {
        this.badge = badge;
    }

    // Full constructor for more detailed model
    public DevotionalAchievementModel(String id, int badge, String badgename, String email, String controlId) {
        this.id = id;
        this.badge = badge;
        this.badgename = badgename;
        this.email = email;
        this.controlId = controlId;
    }

    public String getBadgename() {
        return badgename;
    }

    public void setBadgename(String badgename) {
        this.badgename = badgename;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }
}