package com.example.myapplication.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.biblestories.ModelBible;


@Database(entities = {ModelBible.class, User.class}, version = 2)  // Updated version
public abstract class AppDatabase extends RoomDatabase {
    public abstract BibleDao bibleDao();
    public abstract UserDao userDao();  // Add the UserDao interface

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bible_database")
                            .fallbackToDestructiveMigration()  // Add this to handle schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
