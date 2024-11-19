package com.example.myapplication.ui.content.stories;

import java.util.Date;

public class ModelStories {
    private String title;
    private String type;
    private String verse;
    private String description;
    private String isCompleted;
    private String email;
    private int count;
    private String imageUrl;
    private String id; // This should match the Firestore document ID
    private Date timestamp;
    private String audioUrl;

    // No-argument constructor
    public ModelStories() {
        // Required for Firebase deserialization
    }

    // Constructor with arguments
    public ModelStories(String title, String type, String verse, String email, int count, String imageUrl, String id, Date timestamp,  String description, String isCompleted, String audioUrl) {
        this.title = title;
        this.type = type;
        this.verse = verse;
        this.email = email;
        this.count = count;
        this.imageUrl = imageUrl;
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
        this.isCompleted = isCompleted;
        this.audioUrl = audioUrl;
    }

    // Constructor for when the ID is needed
    public ModelStories(String title, String verse, String description, String imageUrl, String id, String isCompleted, Date timestamp, String audioUrl) {
        this.title = title;
        this.verse = verse;
        this.description = description;
        this.imageUrl = imageUrl;
        this.id = id; // Set the ID here
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.audioUrl = audioUrl;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVerse() {
        return verse;
    }

    public void setVerse(String verse) {
        this.verse = verse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id; // Make sure to set the ID
    }

    public String getDescription() {
        return description;
    }

    public void setRole(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
