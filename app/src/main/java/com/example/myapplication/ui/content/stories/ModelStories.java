package com.example.myapplication.ui.content.stories;

import java.util.Date;

public class ModelStories {
    private String title;
    private String type;
    private String verse;
    private String description;
    private String email;
    private String imageUrl;
    private String id;
    private Date timestamp; // Add a timestamp field

    // No-argument constructor
    public ModelStories() {
        // Required for Firebase deserialization
    }

    // Constructor with arguments
    public ModelStories(String title, String type, String verse, String email, String imageUrl, String id, Date timestamp,String description) {
        this.title = title;
        this.type = type;
        this.verse = verse;
        this.email = email;
        this.imageUrl = imageUrl;
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
    }

    public ModelStories(String title, String verse, String description, String imageUrl, Date timestamp) {
        this.title = title;
        this.verse = verse;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }


    public ModelStories(String title, String description) {
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
        this.title = verse;
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
        this.id = id;
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
