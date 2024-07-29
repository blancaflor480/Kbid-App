package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChildAgeActivity extends AppCompatActivity {

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
                    buttonContinue.setBackgroundColor(getResources().getColor(R.color.purple));
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
                saveAgeToFirebase(childAge);
            }
        });
    }

    private void saveAgeToFirebase(String age) {
        // Generate a unique key for each age
        String key = myRef.push().getKey();
        if (key != null) {
            myRef.child(key).setValue(age).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Name saved successfully, navigate to ChildAgeActivity
                    //Log.d(TAG, "Name saved successfully");
                    Intent intent = new Intent(ChildAgeActivity.this, SkipageActivity.class);
                    startActivity(intent);
                } else {
                    // Handle the error
                    // Log.e(TAG, "Failed to save name: ", task.getException());
                    Toast.makeText(ChildAgeActivity.this, "Failed to save name", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Log.e(TAG, "Failed to generate key");
            Toast.makeText(ChildAgeActivity.this, "Failed to generate key", Toast.LENGTH_SHORT).show();
        }
    }
}
