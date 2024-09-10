package com.example.myapplication.database.fourpicsdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Annotate the class with @Entity to define it as a table in Room
@Entity(tableName = "fourpicsoneword")
public class FourPicsOneWord {

    @PrimaryKey(autoGenerate = true)
    private int id;              // Primary key

    private int userId;          // Foreign key reference to user
    private int currentLevel = 1; // Default level
    private int points = 0;      // Default points
    private String date;         // Date field

    // No-argument constructor
    public FourPicsOneWord() {
        // Optional: Initialize fields with default values if needed
    }

    // Constructor with arguments
    public FourPicsOneWord(int userId, int currentLevel, int points, String date) {
        this.userId = userId;
        this.currentLevel = currentLevel;
        this.points = points;
        this.date = date;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
