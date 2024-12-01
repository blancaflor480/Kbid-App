package com.example.myapplication.fragment.achievement;

public class leaderboardmodel {
    private String userName;
    private String imageUrl;
    private int totalPoints;
    private String rank;

    public leaderboardmodel(String userName, String imageUrl, int totalPoints) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.totalPoints = totalPoints;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
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




