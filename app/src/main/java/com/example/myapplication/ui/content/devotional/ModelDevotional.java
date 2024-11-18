package com.example.myapplication.ui.content.devotional;

import java.util.Date;

public class ModelDevotional {
    private String id;
    private String title;
    private String verse;
    private String memoryverse;
    private String imageUrl;
    private Date timestamp;

    // No-argument constructor
    public ModelDevotional() {
        // Required for Firebase deserialization
    }

    public ModelDevotional(String id, String title, String verse, String memoryverse, String imageUrl,Date timestamp ) {
    this.id = id;
    this.title = title;
    this.verse = verse;
    this.memoryverse = memoryverse;
    this.imageUrl = imageUrl;
    this.timestamp = timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setVerse(String verse) {
        this.verse = verse;
    }
    public String getVerse() {
        return verse;
        }
    public void setMemoryverse(String memoryverse) {
        this.memoryverse = memoryverse;
    }
    public String getMemoryverse() {
        return memoryverse;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
