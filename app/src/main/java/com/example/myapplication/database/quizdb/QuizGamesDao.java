package com.example.myapplication.database.quizdb;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface QuizGamesDao {
    @Insert
    void insert(QuizGames quizGames);

    // Additional DAO methods as needed
}

