package com.example.myapplication.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.myapplication.fragment.biblestories.ModelBible;



@Database(entities = {ModelBible.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BibleDao bibleDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bible_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
