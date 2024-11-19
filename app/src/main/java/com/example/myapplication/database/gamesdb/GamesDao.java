package com.example.myapplication.database.gamesdb;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;

import java.util.List;

@Dao
public interface GamesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Games games);

    @Update
    void update(Games games);

    @Query("SELECT COUNT(*) FROM games WHERE firestoreId = :firestoreId")
    int countByFirestoreId(String firestoreId);

    @Query("SELECT * FROM fourpicsoneword WHERE userId = :userId")
    LiveData<List<FourPicsOneWord>> getAllLevelsForUser(int userId);

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    Games getGameById(int id);

    @Query("SELECT * FROM games")
    LiveData<List<Games>> getAllGames();

    @Query("SELECT * FROM games WHERE level = :level")
    LiveData<List<Games>> getGamesByLevel(int level);

    @Query("DELETE FROM games")
    void deleteAll();

    // Corrected method for returning Cursor
    @Query("SELECT * FROM games")
    Cursor getGameData();  // Ensure this matches the actual database query

    // Methods for updating local image paths
    @Query("UPDATE games SET localImagePath1 = :localPath WHERE firestoreId = :gameId")
    void updateLocalImagePath1(String gameId, String localPath);

    @Query("UPDATE games SET localImagePath2 = :localPath WHERE firestoreId = :gameId")
    void updateLocalImagePath2(String gameId, String localPath);

    @Query("UPDATE games SET localImagePath3 = :localPath WHERE firestoreId = :gameId")
    void updateLocalImagePath3(String gameId, String localPath);

    @Query("UPDATE games SET localImagePath4 = :localPath WHERE firestoreId = :gameId")
    void updateLocalImagePath4(String gameId, String localPath);

}
