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
    private String email, controlid; // Declare email variable

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
        controlid = getIntent().getStringExtra("CONTROL_ID");
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
            String defaultAvatarName = "";
            String childBirthday = "";
            int defaultAvatarResourceId = R.drawable.lion;
            byte[] defaultAvatarImage = null;
            String email = this.email;
            String controlid = this.controlid;

            // Create and insert user with email
            User user = new User(name, childBirthday, defaultAvatarName, defaultAvatarResourceId, defaultAvatarImage, email, controlid);
            long userId = userDao.insert(user);

            // Create FourPicsOneWord with email
            FourPicsOneWord fourPicsOneWord = new FourPicsOneWord(
                    (int) userId,
                    1,
                    0,
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()),
                    email  // Adding email here
            );

            db.fourPicsOneWordDao().insert(fourPicsOneWord);
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
