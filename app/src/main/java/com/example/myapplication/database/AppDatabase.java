package com.example.myapplication.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;


import com.example.myapplication.database.Converters;

import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.gamesdb.Games;
import com.example.myapplication.database.quizdb.QuizGames;
import com.example.myapplication.database.quizdb.QuizGamesDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.biblestories.ModelBible;
import com.example.myapplication.database.gamesdb.GamesDao;
import com.example.myapplication.MainActivity;

@Database(entities = {ModelBible.class, User.class, Games.class, FourPicsOneWord.class, QuizGames.class}, version = 5)  // Updated version
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BibleDao bibleDao();
    public abstract UserDao userDao();
    public abstract GamesDao gamesDao();  // Add the GameDao interface
    public abstract FourPicsOneWordDao fourPicsOneWordDao();
    public abstract QuizGamesDao quizGamesDao();


    private static volatile AppDatabase INSTANCE;
    private static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bible_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
