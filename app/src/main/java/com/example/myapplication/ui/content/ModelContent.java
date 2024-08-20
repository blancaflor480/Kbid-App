package com.example.myapplication.ui.content;

public class ModelContent {
    private String title;
    private String type;
    private String date;
    private String verse;
    private String description;
    private String email;
    private String imageUrl;
    private String id;

    // No-argument constructor
    public ModelContent() {
        // Required for Firebase deserialization
    }

    // Constructor with arguments
    public ModelContent(String title, String type, String date, String verse, String email, String imageUrl, String id, String description) {
        this.title = title;
        this.type = type;
        this.date = date;
        this.verse = verse;
        this.email = email;
        this.imageUrl = imageUrl;
        this.id = id;
        this.description = description;
    }

    public ModelContent(String title, String verse, String description, String imageUrl) {
    }

    public ModelContent(String title, String description) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

}
