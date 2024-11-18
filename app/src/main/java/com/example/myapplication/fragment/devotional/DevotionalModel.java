package com.example.myapplication.fragment.devotional;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;

import java.util.Date;

@Entity(tableName = "devotional")
public class DevotionalModel {

    @PrimaryKey
    @NonNull
    private String id; // ID should be non-null and serve as the primary key

    private String title;
    private String verse;
    private String memoryverse;
    private String reflectionanswer;
    private String imageUrl;

    @TypeConverters(Converters.class)  // Use the custom converter for Timestamp
    private Timestamp timestamp;
    private String formattedTimestamp;

    // No-argument constructor required for Firebase deserialization and Room
    public DevotionalModel() {}

    // Constructor with all arguments
    public DevotionalModel(@NonNull String id, String title, String verse, String memoryverse, String reflectionanswer, String imageUrl, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.verse = verse;
        this.memoryverse = memoryverse;
        this.reflectionanswer = reflectionanswer;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getMemoryverse() {
        return memoryverse;
    }

    public void setMemoryverse(String memoryverse) {
        this.memoryverse = memoryverse;
    }

    public String getReflectionanswer() {
        return reflectionanswer;
    }

    public void setReflectionanswer(String reflectionanswer) {
        this.reflectionanswer = reflectionanswer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
