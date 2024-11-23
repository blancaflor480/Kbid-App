package com.example.myapplication.ui.feedback;

import java.util.Date;

public class FeedbackViewModel{
    private String email;
    private String comment;
    private int rating;
    private Date timestamp;

    public FeedbackViewModel(){

    }

    public FeedbackViewModel(String email, String comment, int rating, Date timestamp){
    this.email = email;
    this.comment = comment;
    this.rating = rating;
    this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}