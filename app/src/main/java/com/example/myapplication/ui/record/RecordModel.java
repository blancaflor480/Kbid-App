package com.example.myapplication.ui.record;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class RecordModel {
    private String email;
    private String imageUrl;
    private String storyId;
    private String gameId;
    private String kidsreflectionId;
    private String rank;
    private String totalachievements;
    private String name;
    private Date timestamp;
    // Getter and Setter methods


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getKidsreflectionId() {
        return kidsreflectionId;
    }

    public void setKidsreflectionId(String kidsreflectionId) {
        this.kidsreflectionId = kidsreflectionId;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRank() {
        return rank;
    }

    public String getTotalachievements() {
        return totalachievements;
    }

    public void setTotalachievements(String totalachievements) {
        this.totalachievements = totalachievements;
    }
}
