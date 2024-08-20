package com.example.myapplication.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.fragment.biblestories.ModelBible;

import java.util.List;

@Dao
public interface BibleDao {
    @Insert
    void insert(ModelBible bibleStory);

    @Query("SELECT * FROM bible_stories")
    List<ModelBible> getAllBibleStories();

    @Query("SELECT * FROM bible_stories WHERE verseName = :name")
    ModelBible getBibleStoryByName(String name);
}
