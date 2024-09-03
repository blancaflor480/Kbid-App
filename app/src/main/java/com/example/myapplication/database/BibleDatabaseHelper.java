package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myapplication.fragment.biblestories.ModelBible;
import com.example.myapplication.ui.content.games.ModelGames;

public class BibleDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kbid-app.db";
    private static final int DATABASE_VERSION = 1; // Incremented version for new fields

    private static final String TABLE_NAME = "stories";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRESTORE_ID = "firestoreId";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_VERSE = "verse";  // New column
    private static final String COLUMN_DATE = "timestamp";  // New column
    private static final String COLUMN_IMAGE_URL = "imageUrl";  // New column

    private static final String TABLE_GAMES = "games";
    private static final String COLUMN__GAME_ID = "id";
    private static final String COLUMN_GAME_TITLE = "title";
    private static final String COLUMN_ANSWER = "answer";
    private static final String COLUMN_GAME_LEVEL = "level";
    private static final String COLUMN_IMAGE_URL1 = "imageUrl1";
    private static final String COLUMN_IMAGE_URL2 = "imageUrl2";
    private static final String COLUMN_IMAGE_URL3 = "imageUrl3";
    private static final String COLUMN_IMAGE_URL4 = "imageUrl4";
    private static final String COLUMN_GAME_DATE = "timestamp";




    private static final String CREATE_STORIES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRESTORE_ID + " TEXT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_VERSE + " TEXT,"
            + COLUMN_DATE + " DATE,"
            + COLUMN_IMAGE_URL + " TEXT" + ")";

    private static final String CREATE_GAMES_TABLE = "CREATE TABLE " + TABLE_GAMES + "("
            + COLUMN__GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_GAME_TITLE + " TEXT,"
            + COLUMN_ANSWER + " TEXT,"
            + COLUMN_GAME_LEVEL + " TEXT,"
            + COLUMN_IMAGE_URL1 + " TEXT,"
            + COLUMN_IMAGE_URL2 + " TEXT,"
            + COLUMN_IMAGE_URL3 + " TEXT,"
            + COLUMN_IMAGE_URL4 + " TEXT,"
            + COLUMN_GAME_DATE + " DATE" + ")";

    public BibleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void insertGame(ModelGames game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GAME_TITLE, game.getTitle());
        values.put(COLUMN_ANSWER, game.getAnswer());
        values.put(COLUMN_GAME_LEVEL, game.getLevel());
        values.put(COLUMN_IMAGE_URL1, game.getImageUrl1());
        values.put(COLUMN_IMAGE_URL2, game.getImageUrl2());
        values.put(COLUMN_IMAGE_URL3, game.getImageUrl3());
        values.put(COLUMN_IMAGE_URL4, game.getImageUrl4());

        // Convert Date to Long (timestamp in milliseconds)
        if (game.getTimestamp() != null) {
            values.put(COLUMN_GAME_DATE, game.getTimestamp().getTime());
        } else {
            values.put(COLUMN_GAME_DATE, (Long) null); // Handle null case if needed
        }

        long result = db.insert(TABLE_GAMES, null, values);
        if (result == -1) {
            Log.e("BibleDatabaseHelper", "Failed to insert game data");
        } else {
            Log.d("BibleDatabaseHelper", "Game data inserted successfully");
        }
        db.close();
    }


    public ModelBible getNextStory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ModelBible story = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " > ? ORDER BY " + COLUMN_ID + " LIMIT 1", new String[]{String.valueOf(id)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                story = new ModelBible();
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                int verseIndex = cursor.getColumnIndex(COLUMN_VERSE);
                int imageUrlIndex = cursor.getColumnIndex(COLUMN_IMAGE_URL);

                // Ensure indexes are valid before accessing the cursor
                if (idIndex != -1) {
                    story.setId(String.valueOf(cursor.getInt(idIndex))); // Convert int to String
                }
                if (titleIndex != -1) {
                    story.setTitle(cursor.getString(titleIndex));
                }
                if (descriptionIndex != -1) {
                    story.setDescription(cursor.getString(descriptionIndex));
                }
                if (verseIndex != -1) {
                    story.setVerse(cursor.getString(verseIndex));
                }
                if (imageUrlIndex != -1) {
                    story.setImageUrl(cursor.getString(imageUrlIndex));
                }
            }
            cursor.close();
        }
        return story; // Return null if no next story is found
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STORIES_TABLE);
        db.execSQL(CREATE_GAMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // You can implement a more sophisticated upgrade strategy here
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);

            onCreate(db);
        }
    }
}
