package com.example.myapplication.database.devotionaldb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.devotional.DevotionalModel;

import java.util.List;

@Dao
public interface DevotionalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DevotionalModel devotional);

    @Query("SELECT * FROM devotional WHERE id = :id")
    DevotionalModel getDevotionalById(String id);

    @Query("SELECT * FROM devotional")
    List<DevotionalModel> getAllDevotionals();
}
