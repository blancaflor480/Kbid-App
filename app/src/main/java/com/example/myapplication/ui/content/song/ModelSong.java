package com.example.myapplication.ui.content.song;
import java.util.Date;

public class ModelSong {
    private String title;
    private String song;
    private String description;
    private String email;
    private String imageUrl;
    private String id; // This should match the Firestore document ID
    private Date timestamp;
    private String videoUrl;


    // No-argument constructor
    public ModelSong() {
        // Required for Firebase deserialization
    }
    // Constructor with arguments
    public ModelSong(String title, String song, String videoUrl, String description, String email, String imageUrl, Object o, Date timestamp) {
        this.title = title;
        this.song = song;
        this.videoUrl = videoUrl;
        this.email = email;
        this.imageUrl = imageUrl;
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
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

}
