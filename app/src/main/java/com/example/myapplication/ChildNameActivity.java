package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

public class ChildNameActivity extends AppCompatActivity {

    private static final String TAG = "ChildNameActivity";

    private EditText inputName;
    private Button buttonContinue;
    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_name);

        inputName = findViewById(R.id.inputName);
        buttonContinue = findViewById(R.id.buttonContinue);

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Set initial state of the button
        buttonContinue.setEnabled(false);
        buttonContinue.setBackgroundColor(Color.GRAY);
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
                    buttonContinue.setBackgroundColor(Color.GRAY);
                    buttonContinue.setTextColor(Color.BLACK);
                } else {
                    buttonContinue.setEnabled(true);
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.greenlightning));
                    buttonContinue.setTextColor(Color.WHITE);
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
            String defaultAvatarName = "Default Avatar";
            int defaultAvatarResourceId = R.drawable.lion; // Ensure you have this drawable in your project

            byte[] defaultAvatarImage = null; // Or replace this with actual image data if available
            User user = new User(name, name, defaultAvatarName, defaultAvatarResourceId, defaultAvatarImage);
            userDao.insert(user);
        });
    }

    private void loadSavedName() {
        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null) {
                runOnUiThread(() -> {
                    inputName.setText(user.getChildName());
                    buttonContinue.setEnabled(true);
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.greenlightning));
                    buttonContinue.setTextColor(Color.WHITE);
                });
            }
        });
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(ChildNameActivity.this, ChildAgeActivity.class);
        startActivity(intent);
    }
}
