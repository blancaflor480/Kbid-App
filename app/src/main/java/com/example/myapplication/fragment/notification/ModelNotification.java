package com.example.myapplication.fragment.notification;

public class ModelNotification {
    private String announcement;
    private String date;
    private String imageUrl; // Optional: if you have an image URL

    public ModelNotification(String announcement, String date, String imageUrl) {
        this.announcement = announcement;
        this.date = date;
        this.imageUrl = imageUrl;
    }
    public String getAnnouncement() {
        return announcement;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

