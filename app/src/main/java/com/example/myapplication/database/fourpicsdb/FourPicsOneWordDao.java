package com.example.myapplication.database.fourpicsdb;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface FourPicsOneWordDao {

    @Insert
    void insert(FourPicsOneWord fourPicsOneWord);

    @Query("SELECT * FROM fourpicsoneword WHERE userId = :userId LIMIT 1")
    LiveData<FourPicsOneWord> getCurrentLevel(int userId);

    @Query("UPDATE fourpicsoneword SET points = points + :points WHERE userId = :userId")
    void addPoints(int userId, int points);

    @Query("UPDATE fourpicsoneword SET currentLevel = currentLevel + :currentLevel WHERE userId = :userId")
    void addLevel(int userId, int currentLevel);
    // Additional DAO methods can be defined here as needed
}
