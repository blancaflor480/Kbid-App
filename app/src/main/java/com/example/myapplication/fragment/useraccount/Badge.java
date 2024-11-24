package com.example.myapplication.fragment.useraccount;

public class Badge {
    private final int badgeImage;
    private final boolean isCompleted;
    private final boolean isUnlocked;
    private final boolean hasStarReward;

    public Badge(int badgeImage, boolean isCompleted, boolean isUnlocked, boolean hasStarReward) {
        this.badgeImage = badgeImage;
        this.isCompleted = isCompleted;
        this.isUnlocked = isUnlocked;
        this.hasStarReward = hasStarReward;
    }

    public int getBadgeImage() {
        return badgeImage;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public boolean hasStarReward() {
        return hasStarReward;
    }
}

