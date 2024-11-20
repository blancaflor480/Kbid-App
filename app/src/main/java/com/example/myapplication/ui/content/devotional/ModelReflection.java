package com.example.myapplication.ui.content.devotional;

import java.util.Date;

public class ModelReflection extends ModelDevotional {
    private String id;
    private String email;
    private String reflectionanswer;
    private Date timestamp;
    private String ImageUrl;
    private String controlId;

    public ModelReflection() {}

    public ModelReflection(String id, String email, String reflectionanswer, Date timestamp,String ImageUrl, String controlId) {
        this.id = id;
        this.email = email;
        this.reflectionanswer = reflectionanswer;
        this.timestamp = timestamp;
        this.ImageUrl = ImageUrl;
        this.controlId = controlId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getReflectionanswer() {
        return reflectionanswer;
    }

    public void setReflectionanswer(String reflectionanswer) {
        this.reflectionanswer = reflectionanswer;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getImageUrl() {
        return ImageUrl;
    }
    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }
}
