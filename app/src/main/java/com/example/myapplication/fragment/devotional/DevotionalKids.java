package com.example.myapplication.fragment.devotional;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.Notification.NotificationHelper;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DevotionalKids extends AppCompatActivity {

    TextView memoryverse, verse;
    AppCompatButton submit;
    EditText answerreflection;
    ImageView devotionalThumbnail;
    SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseFirestore db;
    private String devotionalId;
    private AppDatabase appDatabase;
    private TextToSpeech textToSpeech;
    private AppCompatButton speechToTextButton;

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
        speechToTextButton = findViewById(R.id.speechttext);
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

        speechToTextButton.setOnClickListener(v -> startSpeechToText());

        // Setup SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> loadDevotional(devotionalId));

        // Handle submit button click
        submit.setOnClickListener(v -> {
            String reflection = answerreflection.getText().toString().trim();
            if (!reflection.isEmpty()) {
                String email = getIntent().getStringExtra("email");
                String controlId = getIntent().getStringExtra("controlId");

                if (email != null && controlId != null) {
                    checkAndSubmitReflection(devotionalId, reflection, email, controlId);
                } else {
                    Toast.makeText(DevotionalKids.this, "Email or Control ID is missing.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DevotionalKids.this, "Please enter your reflection.", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(Locale.US);
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language is not supported or missing data");
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
    }

    // Method to start Speech Recognition
    private void startSpeechToText() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Start activity for result
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                answerreflection.setText(results.get(0));
            }
        }
    }

    // Fetch first devotional ID
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

    // Load devotional data from Firestore
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
                            updateUIWithDevotional(devotional);
                            saveDevotionalToLocal(devotional);
                            checkReflectionStatus(devotionalId);
                        }
                    } else {
                        Toast.makeText(DevotionalKids.this, "No such document found.", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error fetching document from Firestore", e);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    // Update UI with devotional data
    private void updateUIWithDevotional(DevotionalModel devotional) {
        if (devotional == null) return;

        memoryverse.setText(devotional.getMemoryverse());
        verse.setText(devotional.getVerse());

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

    // Check if reflection has already been submitted
    private void checkReflectionStatus(String devotionalId) {
        String controlId = getIntent().getStringExtra("controlId");
        db.collection("kidsReflection")
                .whereEqualTo("devotionalId", devotionalId)
                .whereEqualTo("controlId", controlId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        submit.setEnabled(false);
                        submit.setBackgroundColor(getResources().getColor(R.color.gray)); // Gray button
                    }
                })
                .addOnFailureListener(e -> Log.e("DevotionalKids", "Error checking reflection status", e));
    }

    private void checkAndSubmitReflection(String devotionalId, String reflectionText, String email, String controlId) {
        // Step 1: Check if reflection already exists for the given devotionalId and controlId
        db.collection("kidsReflection")
                .whereEqualTo("devotionalId", devotionalId)
                .whereEqualTo("controlId", controlId)
                .whereEqualTo("email", email) // Validate email as well
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Step 2: If no existing reflection, proceed with submission
                        submitReflection(devotionalId, reflectionText, email, controlId);
                    } else {
                        // Step 3: Reflection already submitted, disable button and show message
                        Toast.makeText(DevotionalKids.this, "You have already submitted your reflection.", Toast.LENGTH_SHORT).show();
                        submit.setEnabled(false);
                        submit.setText("Submitted");
                        submit.setTextColor(getResources().getColor(R.color.white));
                        submit.setBackground(getResources().getDrawable(R.drawable.shadow3dbutton));
                        submit.setBackgroundColor(getResources().getColor(R.color.gray));  // Gray out button
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking reflection existence", e);
                    Toast.makeText(DevotionalKids.this, "Failed to check reflection status.", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitReflection(String devotionalId, String reflectionText, String email, String controlId) {
        // Step 1: Create a new reflection object
        DevotionalModel reflection = new DevotionalModel();
        reflection.setId(devotionalId);
        reflection.setReflectionanswer(reflectionText);
        reflection.setTimestamp(new Timestamp(new Date())); // Capture timestamp
        reflection.setEmail(email);
        reflection.setControlId(controlId);

        // Step 2: Save reflection in Firestore
        db.collection("kidsReflection")
                .add(reflection)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DevotionalKids.this, "Reflection submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Clear input field and update UI
                    answerreflection.setText("");
                    disableSubmitButton();
                    showSubmissionSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error submitting reflection", e);
                    Toast.makeText(DevotionalKids.this, "Failed to submit reflection. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void disableSubmitButton() {
        submit.setEnabled(false);
        submit.setText("Submitted");
        submit.setTextColor(getResources().getColor(R.color.white));
        submit.setBackground(getResources().getDrawable(R.drawable.shadow3dbutton));
        submit.setBackgroundColor(getResources().getColor(R.color.gray));  // Gray out button
    }

    private void showSubmissionSuccessDialog() {
        LayoutInflater inflater = LayoutInflater.from(DevotionalKids.this);
        View dialogView = inflater.inflate(R.layout.submitted, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(DevotionalKids.this);
        builder.setView(dialogView);
        builder.setCancelable(false); // Optionally make it non-cancellable

        AlertDialog successDialog = builder.create();
        successDialog.show();

        new Handler().postDelayed(successDialog::dismiss, 2000);  // Dismiss after 2 seconds
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
