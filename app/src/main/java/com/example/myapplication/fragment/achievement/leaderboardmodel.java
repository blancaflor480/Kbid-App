package com.example.myapplication.fragment.achievement;

public class leaderboardmodel {
    private String userName;
    private String imageUrl;
    private int totalPoints;

    public leaderboardmodel(String userName, String imageUrl, int totalPoints) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.totalPoints = totalPoints;
    }

    public String getUserName() {
        return userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTotalPoints() {
        return totalPoints;
    }
}




