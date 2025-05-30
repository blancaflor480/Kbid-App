package com.example.myapplication.fragment.biblestories;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;

@Entity(tableName = "stories")
@TypeConverters({Converters.class})
public class ModelBible {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String description;
    private String verse;
    private String timestamp; // Ensure Converters can handle this as a String
    private String imageUrl;
    private String audioUrl;
    private String isCompleted;
    private int count;
    private boolean isAudioDownloaded;

    // No-argument constructor required by Firestore
    public ModelBible() {}

    // All-arguments constructor
    public ModelBible(@NonNull String id, String title, String description, String verse, String timestamp,
                      String imageUrl, String audioUrl, String isCompleted, int count, boolean isAudioDownloaded) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.verse = verse;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.isCompleted = isCompleted;
        this.count = count;
        this.isAudioDownloaded = isAudioDownloaded;
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVerse() {
        return verse;
    }

    public void setVerse(String verse) {
        this.verse = verse;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isAudioDownloaded() { // Updated method name
        return isAudioDownloaded;
    }

    public void setAudioDownloaded(boolean isAudioDownloaded) {
        this.isAudioDownloaded = isAudioDownloaded;
    }
}
