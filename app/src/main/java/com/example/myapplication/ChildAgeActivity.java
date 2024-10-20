package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

public class ChildAgeActivity extends AppCompatActivity {

    private EditText inputAge;
    private Button buttonContinue;
    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_age);

        inputAge = findViewById(R.id.inputAge);
        buttonContinue = findViewById(R.id.buttonContinue);

        // Ensure the input field is empty at the start
        inputAge.setText(""); // Clears any pre-filled data

        // Set input filter to allow numbers only
        inputAge.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        inputAge.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)}); // Adjust length as needed

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Set initial state of the button
        buttonContinue.setEnabled(false);
        buttonContinue.setBackgroundColor(Color.GRAY);
        buttonContinue.setTextColor(Color.BLACK);

        // Add TextWatcher to EditText
        inputAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable button only if input is not empty and is numeric
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
                // Validate input as numeric
                if (!s.toString().matches("\\d*")) {
                    Toast.makeText(ChildAgeActivity.this, "Please enter numbers only", Toast.LENGTH_SHORT).show();
                    inputAge.setText("");
                }
            }
        });

        // Set OnClickListener to the button
        buttonContinue.setOnClickListener(v -> {
            String childAge = inputAge.getText().toString().trim();
            if (!childAge.isEmpty()) {
                saveOrUpdateUser(childAge);
                proceedToNextActivity();
            }
        });

        // Load user data if needed
        loadSavedUser();
    }

    private void saveOrUpdateUser(String age) {
        AsyncTask.execute(() -> {
            if (currentUser != null) {
                // User already exists, update with new age
                currentUser.setChildAge(age);
                userDao.updateUser(currentUser);
            } else {
                // No user exists, create a new one with default name and provided age
                byte[] defaultAvatarImage = null; // Replace with actual byte array if you have an image
                String email = "No Bind";
                currentUser = new User("Default Name", age, "Default Avatar", R.drawable.lion, defaultAvatarImage, email);

                // Replace "Default Name" with the actual child's name if needed
                userDao.insert(currentUser);
            }
        });
    }

    private void loadSavedUser() {
        AsyncTask.execute(() -> {
            currentUser = userDao.getFirstUser();
            if (currentUser != null) {
                runOnUiThread(() -> {
                    // Ensure the input field is empty to show the hint
                    inputAge.setText("");
                });
            }
        });
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(ChildAgeActivity.this, SkipageActivity.class);
        startActivity(intent);
    }
}
