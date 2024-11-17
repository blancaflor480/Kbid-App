package com.example.myapplication.database.music;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.biblemusic.ModelMusic;

import java.util.List;

@Dao // Add the @Dao annotation here
public interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ModelMusic music); // Insert method for ModelMusic objects

    @Query("SELECT * FROM video WHERE timestamp >= :currentDate ORDER BY timestamp ASC")
    List<ModelMusic> getUpcomingBibleMusic(String currentDate); // Query to get upcoming music

    @Query("SELECT * FROM video")
    List<ModelMusic> getAllBibleMusic(); // Query to get all music

    @Query("SELECT * FROM video WHERE id = :songId LIMIT 1")
    ModelMusic getStoryById(String songId); // Query to get a specific music by its ID

    // Annotate deleteAll() with a DELETE query to clear the table
    @Query("DELETE FROM video")
    void deleteAll();

}
