package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.quizdb.QuizGames;
import com.example.myapplication.database.quizdb.QuizGamesDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildNameActivity extends AppCompatActivity {

    private static final String TAG = "ChildNameActivity";

    private EditText inputName;
    private Button buttonContinue;
    private AppDatabase db;
    private UserDao userDao;
    private QuizGamesDao quizGamesDao;
    private FourPicsOneWordDao fourPicsOneWordDao;
    private String email; // Declare email variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_name);

        inputName = findViewById(R.id.inputName);
        buttonContinue = findViewById(R.id.buttonContinue);

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        quizGamesDao = db.quizGamesDao();
        fourPicsOneWordDao = db.fourPicsOneWordDao();

        // Initialize the database and DAO
        email = getIntent().getStringExtra("USER_EMAIL");
        //email = getIntent().getStringExtra("email");


        // Set initial state of the button
        buttonContinue.setEnabled(false);
        buttonContinue.setBackgroundColor(getResources().getColor(R.color.gray));
        buttonContinue.setBackgroundResource(R.drawable.btn_disbaled);
        buttonContinue.setTextColor(Color.BLACK);

        // Add TextWatcher to EditText
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if EditText is empty
                if (s.toString().trim().isEmpty()) {
                    buttonContinue.setEnabled(false);
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.gray));
                    buttonContinue.setBackgroundResource(R.drawable.btn_disbaled);
                    buttonContinue.setTextColor(Color.BLACK);

                } else {
                    buttonContinue.setEnabled(true);
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.green));
                    buttonContinue.setTextColor(Color.WHITE);
                    buttonContinue.setBackgroundResource(R.drawable.btn_getstarted);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Set OnClickListener to the button
        buttonContinue.setOnClickListener(v -> {
            String childName = inputName.getText().toString().trim();
            if (!childName.isEmpty()) {
                saveNameLocally(childName);
                proceedToNextActivity();
            }
        });

        // Check if there is a locally saved name
        loadSavedName();
    }

   private void saveNameLocally(String name) {
        AsyncTask.execute(() -> {
            // Default values for the avatar
            String defaultAvatarName = "";
            String childBirthday = "";
            int defaultAvatarResourceId = R.drawable.lion; // Ensure you have this drawable in your project
            byte[] defaultAvatarImage = null; // Or replace this with actual image data if available

            // Set email to null
            String email = this.email;

            // Create and insert the user
            User user = new User(name, childBirthday, defaultAvatarName, defaultAvatarResourceId, defaultAvatarImage, email);
            long userId = userDao.insert(user); // Assuming this returns the user ID

            // Insert into fourpicsoneword table
            FourPicsOneWord fourPicsOneWord = new FourPicsOneWord();
            fourPicsOneWord.setUserId((int) userId);
            // Create the FourPicsOneWord object with the correct constructor
            fourPicsOneWord = new FourPicsOneWord((int) userId, 1, 0, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            // Insert into quizgames table
            QuizGames quizGames = new QuizGames();
            quizGames.setUserId((int) userId);
            quizGames.setScore(0);  // Setting default score or as needed
            quizGames.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())); // Set the current date

            // Insert into respective tables
            db.fourPicsOneWordDao().insert(fourPicsOneWord);
            db.quizGamesDao().insert(quizGames);
        });
    }



    private void loadSavedName() {
        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null) {
                runOnUiThread(() -> {
                    inputName.setText(user.getChildName());
                    buttonContinue.setEnabled(true);
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.gray));
                    buttonContinue.setBackgroundResource(R.drawable.btn_disbaled);
                    buttonContinue.setTextColor(Color.BLACK);
                });
            }
        });
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(ChildNameActivity.this, ChildAgeActivity.class);
        startActivity(intent);
        // Add a smooth slide transition effect
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
