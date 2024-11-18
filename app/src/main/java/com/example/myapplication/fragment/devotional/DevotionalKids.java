package com.example.myapplication.fragment.devotional;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.concurrent.Executors;

public class DevotionalKids extends AppCompatActivity {

    TextView memoryverse, verse;
    AppCompatButton submit;
    EditText answerreflection;
    ImageView devotionalThumbnail;
    SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseFirestore db;
    private String devotionalId; // ID of the devotional being shown
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kidsdevotional);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        // Link views
        memoryverse = findViewById(R.id.memoryverse);
        verse = findViewById(R.id.verse);
        devotionalThumbnail = findViewById(R.id.devotionalThumbnail);
        answerreflection = findViewById(R.id.answerreflection);
        submit = findViewById(R.id.submit);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Retrieve `devotionalId` from Intent, if available
        devotionalId = getIntent().getStringExtra("devotionalId");

        // Fetch devotional if not provided
        if (devotionalId == null) {
            fetchFirstDevotionalId();
        } else {
            loadDevotional(devotionalId);
        }

        // Setup SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> loadDevotional(devotionalId));

        // Handle submit button click
        submit.setOnClickListener(v -> {
            String reflection = answerreflection.getText().toString().trim();
            if (!reflection.isEmpty()) {
                submitReflection(devotionalId, reflection);
            } else {
                Toast.makeText(DevotionalKids.this, "Please enter your reflection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch the first devotional ID
    private void fetchFirstDevotionalId() {
        db.collection("devotional")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        devotionalId = querySnapshot.getDocuments().get(0).getId();
                        loadDevotional(devotionalId);
                    } else {
                        Toast.makeText(this, "No devotionals found in the database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error fetching devotional ID", e);
                    Toast.makeText(this, "Failed to fetch devotional ID. Please check your connection.", Toast.LENGTH_SHORT).show();
                });
    }

    // Load devotional from Firestore
    private void loadDevotional(String id) {
        if (id == null) {
            Toast.makeText(this, "No devotional ID available.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        db.collection("devotional").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DevotionalModel devotional = documentSnapshot.toObject(DevotionalModel.class);

                        if (devotional != null) {
                            devotional.setId(id);

                            // Format the timestamp using the converter
                            if (devotional.getTimestamp() != null) {
                                String formattedTimestamp = Converters.fromTimestampToString(devotional.getTimestamp().toDate().getTime());
                                devotional.setFormattedTimestamp(formattedTimestamp);
                            }

                            updateUIWithDevotional(devotional);
                            saveDevotionalToLocal(devotional);
                        }
                    } else {
                        Toast.makeText(DevotionalKids.this, "No such document found.", Toast.LENGTH_SHORT).show();
                        loadDevotionalFromLocal(id);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error fetching document from Firestore", e);
                    loadDevotionalFromLocal(id);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }


    // Load devotional from local storage (Room database)
    private void loadDevotionalFromLocal(String id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DevotionalModel localDevotional = appDatabase.devotionalDao().getDevotionalById(id);
            runOnUiThread(() -> {
                if (localDevotional != null) {
                    updateUIWithDevotional(localDevotional);
                } else {
                    Toast.makeText(DevotionalKids.this, "Failed to load devotional from local storage.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Update UI with devotional data
    private void updateUIWithDevotional(DevotionalModel devotional) {
        if (devotional == null) {
            Log.e("DevotionalKids", "Devotional is null!");
            return;
        }

        // Set memory verse and verse text
        memoryverse.setText(devotional.getMemoryverse());
        verse.setText(devotional.getVerse());

        // Load image using Glide
        if (devotional.getImageUrl() != null && !devotional.getImageUrl().isEmpty()) {
            Glide.with(DevotionalKids.this)
                    .load(devotional.getImageUrl())
                    .placeholder(R.drawable.image)
                    .into(devotionalThumbnail);
        } else {
            devotionalThumbnail.setImageResource(R.drawable.image);
        }
    }

    // Save devotional locally in Room database
    private void saveDevotionalToLocal(DevotionalModel devotional) {
        Executors.newSingleThreadExecutor().execute(() -> appDatabase.devotionalDao().insert(devotional));
    }

    // Submit reflection answer
    private void submitReflection(String devotionalId, String reflectionText) {
        if (devotionalId == null) {
            Toast.makeText(this, "Devotional not loaded properly.", Toast.LENGTH_SHORT).show();
            return;
        }

        DevotionalModel reflection = new DevotionalModel();
        reflection.setId(devotionalId);
        reflection.setReflectionanswer(reflectionText);
        reflection.setTimestamp((new Timestamp(new Date())));

        db.collection("kidsReflection")
                .add(reflection)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DevotionalKids.this, "Reflection submitted successfully!", Toast.LENGTH_SHORT).show();
                    answerreflection.setText(""); // Clear the reflection input after submission
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error submitting reflection", e);
                    Toast.makeText(DevotionalKids.this, "Failed to submit reflection. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
