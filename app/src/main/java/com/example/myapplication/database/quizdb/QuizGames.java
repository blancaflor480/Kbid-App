package com.example.myapplication.database.quizdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quizgames")
public class QuizGames {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private int score = 0; // Default score
    private String date;   // Date field

    // No-argument constructor (if needed)
    public QuizGames() {
    }

    // Constructor with arguments (if needed)
    public QuizGames(int userId, int score, String date) {
        this.userId = userId;
        this.score = score;
        this.date = date;
    }

    // Getters and Setters
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
