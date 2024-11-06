package com.example.myapplication.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;
import java.util.concurrent.Executor;


import com.example.myapplication.database.Converters;

import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.favorite.FavoriteDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.gamesdb.Games;
import com.example.myapplication.database.quizdb.QuizGames;
import com.example.myapplication.database.quizdb.QuizGamesDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.biblestories.ModelBible;
import com.example.myapplication.database.gamesdb.GamesDao;
import com.example.myapplication.MainActivity;
import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;

@Database(entities = {ModelBible.class, User.class, Games.class, FourPicsOneWord.class, QuizGames.class, Modelfavoritelist.class, StoryAchievementModel.class}, version = 7)  // Updated version
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BibleDao bibleDao();
    public abstract UserDao userDao();
    public abstract GamesDao gamesDao();  // Add the GameDao interface
    public abstract FourPicsOneWordDao fourPicsOneWordDao();
    public abstract QuizGamesDao quizGamesDao();
    public abstract FavoriteDao FavoriteDao();
    public abstract StoryAchievementDao storyAchievementDao();

    private static volatile AppDatabase INSTANCE;
    public static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bible_database")
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Enable foreign key constraints
                                    db.execSQL("PRAGMA foreign_keys = ON;");
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
