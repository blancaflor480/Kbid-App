package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChildAgeActivity extends AppCompatActivity {

    private static final String TAG = "ChildAgeActivity";
    private static final String PREFS_NAME = "ChildAgePrefs";
    private static final String CHILD_AGE_KEY = "childAge";

    private FirebaseDatabase myDb;
    private DatabaseReference myRef;
    private EditText inputAge;
    private Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_age);

        inputAge = findViewById(R.id.inputAge);
        buttonContinue = findViewById(R.id.buttonContinue);

        // Enable Firebase offline capabilities
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
        }

        // Initialize Firebase database reference
        myDb = FirebaseDatabase.getInstance();
        myRef = myDb.getReference("childAge");

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
            String childAge = inputAge.getText().toString().trim();
            if (!childAge.isEmpty()) {
                saveAgeLocally(childAge);
                saveAgeToFirebase(childAge);
                proceedToNextActivity();
            }
        });

        // Check if there is a locally saved age
        String savedAge = getSavedAge();
        if (savedAge != null) {
            inputAge.setText(savedAge);
            buttonContinue.setEnabled(true);
            buttonContinue.setBackgroundColor(getResources().getColor(R.color.greenlightning));
            buttonContinue.setTextColor(Color.WHITE);
        }
    }

    private void saveAgeLocally(String age) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHILD_AGE_KEY, age);
        editor.apply();
    }

    private String getSavedAge() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(CHILD_AGE_KEY, null);
    }

    private void saveAgeToFirebase(String age) {
        if (isNetworkAvailable()) {
            // Generate a unique key for each age
            String key = myRef.push().getKey();
            if (key != null) {
                myRef.child(key).setValue(age).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Age saved successfully to Firebase");
                    } else {
                        Log.e(TAG, "Failed to save age to Firebase", task.getException());
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save age to Firebase: ", e);
                });
            } else {
                Log.e(TAG, "Failed to generate key");
            }
        } else {
            Log.w(TAG, "No network connection, saving age locally only");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(ChildAgeActivity.this, SkipageActivity.class);
        startActivity(intent);
    }
}
