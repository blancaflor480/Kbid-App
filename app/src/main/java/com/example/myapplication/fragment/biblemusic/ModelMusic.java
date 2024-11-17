package com.example.myapplication.fragment.biblemusic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;

import java.util.Date;

@TypeConverters({Converters.class})
@Entity(tableName = "video") // You can specify the table name if you like
public class ModelMusic {

    @PrimaryKey(autoGenerate = true) // If you want Room to auto-generate the ID
    private int id; // Use an int or long for Room's primary key

    private String firebaseId;
    private String title;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private String timestamp;

    // Constructor with arguments
    public ModelMusic(String firebaseId, String title, String videoUrl, String description, String imageUrl, String timestamp) {
        this.firebaseId = firebaseId;
        this.title = title;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.description = description;
    }

    // Getters and setters
    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

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

    public String getVideoUrl() { // Corrected getter name
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) { // Corrected setter name
        this.videoUrl = videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
