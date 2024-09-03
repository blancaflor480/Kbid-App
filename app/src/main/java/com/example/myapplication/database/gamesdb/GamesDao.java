package com.example.myapplication.database.gamesdb;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.myapplication.database.gamesdb.DataFetcher;
@Dao
public interface GamesDao {

    @Insert
    void insert(Games games);

    @Update
    void update(Games games);

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    Games getGameById(int id);

    @Query("SELECT * FROM games")
    LiveData<List<Games>> getAllGames();

    @Query("DELETE FROM games")
    void deleteAll();

    // Corrected method for returning Cursor
    @Query("SELECT * FROM games")
    Cursor getGameData();  // Ensure this matches the actual database query


}
