package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.biblestories.ModelBible;

import java.util.List;

@Dao
public interface BibleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ModelBible story); // This method handles inserting a ModelBible object

    @Query("SELECT * FROM stories")
    List<ModelBible> getAllBibleStories();

}
