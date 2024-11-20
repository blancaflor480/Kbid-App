package com.example.myapplication.fragment.devotional;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlarmManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.Notification.NotificationHelper;
import com.example.myapplication.Notification.NotificationReceiver;
import com.example.myapplication.R;
import com.example.myapplication.SignupUser;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.Calendar;
public class DevotionalKids extends AppCompatActivity {

    TextView memoryverse, verse;
    AppCompatButton submit;
    EditText answerreflection;
    ImageView devotionalThumbnail,arrowback;
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
        arrowback = findViewById(R.id.arrowback);
        arrowback.setOnClickListener(v -> onBackPressed());
        memoryverse = findViewById(R.id.memoryverse);
        verse = findViewById(R.id.verse);
        devotionalThumbnail = findViewById(R.id.devotionalThumbnail);
        answerreflection = findViewById(R.id.answerreflection);
        speechToTextButton = findViewById(R.id.speechttext);
        submit = findViewById(R.id.submit);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Retrieve `devotionalId` from Intent, if available
        devotionalId = getIntent().getStringExtra("devotionalId");
        String controlId = getIntent().getStringExtra("controlId");
        String email = getIntent().getStringExtra("email");
        // Fetch devotional if not provided
        if (devotionalId == null) {
            fetchTodayDevotional();
        } else {
            loadDevotional(devotionalId, controlId, email);
        }

        updateCardColors();
        speechToTextButton.setOnClickListener(v -> startSpeechToText());

        // Set up the alarm for 6 AM daily
        scheduleDailyNotification();
        // Setup SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> loadDevotional(devotionalId, controlId, email));

        // Handle submit button click
        submit.setOnClickListener(v -> {
            String reflection = answerreflection.getText().toString().trim();
            if (!reflection.isEmpty()) {

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

    private void scheduleDailyNotification() {
        // Get Calendar instance for 6 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed for today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Create an intent to send the notification
        Intent intent = new Intent(this, NotificationReceiver.class);

        // Use FLAG_IMMUTABLE since the PendingIntent doesn't need to be modified
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set a repeating alarm for every day at 6 AM
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, // Repeat daily
                    pendingIntent);
        } else {
            Log.e("DevotionalKids", "AlarmManager is null");
        }
    }

    private void updateCardColors() {
        // Get the current day of the week
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday, ..., 7 = Saturday

        // Reset all card backgrounds to default (white) and text colors to black
        resetCardViews();

        // Set the background color and text color based on the current day
        int selectedColor = getResources().getColor(R.color.blue); // Color for the current day
        int selectedTextColor = getResources().getColor(R.color.white); // White text for selected day

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                setCardViewColors(R.id.mon, selectedColor, selectedTextColor);
                break;
            case Calendar.TUESDAY:
                setCardViewColors(R.id.tue, selectedColor, selectedTextColor);
                break;
            case Calendar.WEDNESDAY:
                setCardViewColors(R.id.wed, selectedColor, selectedTextColor);
                break;
            case Calendar.THURSDAY:
                setCardViewColors(R.id.thu, selectedColor, selectedTextColor);
                break;
            case Calendar.FRIDAY:
                setCardViewColors(R.id.fri, selectedColor, selectedTextColor);
                break;
            case Calendar.SATURDAY:
                setCardViewColors(R.id.sat, selectedColor, selectedTextColor);
                break;
            case Calendar.SUNDAY:
                setCardViewColors(R.id.sun, selectedColor, selectedTextColor);
                break;
        }
    }

    private void resetCardViews() {
        // Reset all card backgrounds to white and text colors to black
        ((CardView) findViewById(R.id.mon)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.tue)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.wed)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.thu)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.fri)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.sat)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.sun)).setCardBackgroundColor(getResources().getColor(R.color.white));

        // Set default text color (black) for all days
        setTextColor(R.id.mon, getResources().getColor(R.color.black));
        setTextColor(R.id.tue, getResources().getColor(R.color.black));
        setTextColor(R.id.wed, getResources().getColor(R.color.black));
        setTextColor(R.id.thu, getResources().getColor(R.color.black));
        setTextColor(R.id.fri, getResources().getColor(R.color.black));
        setTextColor(R.id.sat, getResources().getColor(R.color.black));
        setTextColor(R.id.sun, getResources().getColor(R.color.black));
    }

    private void setCardViewColors(int cardId, int backgroundColor, int textColor) {
        CardView cardView = findViewById(cardId);
        cardView.setCardBackgroundColor(backgroundColor);

        // Get the TextView inside the CardView and change its text color
        TextView textView = (TextView) cardView.getChildAt(0); // Assuming the TextView is the first child
        textView.setTextColor(textColor);
    }

    private void setTextColor(int cardId, int textColor) {
        CardView cardView = findViewById(cardId);
        TextView textView = (TextView) cardView.getChildAt(0); // Assuming the TextView is the first child
        textView.setTextColor(textColor);
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
    private void fetchTodayDevotional() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date todayStart = calendar.getTime();

        // Query to fetch devotional entries sorted by timestamp
        db.collection("devotional")
                .whereGreaterThanOrEqualTo("timestamp", todayStart) // Get devotionals after today's start
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1) // Get the closest devotional for today
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        devotionalId = querySnapshot.getDocuments().get(0).getId();
                        String controlId = getIntent().getStringExtra("controlId");
                        String email = getIntent().getStringExtra("email");
                        loadDevotional(devotionalId, controlId, email);
                    } else {
                        Toast.makeText(this, "No devotionals available for today.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error fetching today's devotional", e);
                    Toast.makeText(this, "Failed to fetch devotional for today. Please check your connection.", Toast.LENGTH_SHORT).show();
                });
    }


    // Load devotional data from Firestore
    private void loadDevotional(String id, String controlId, String email) {
        if (id == null) {
            Toast.makeText(this, "No devotional ID available.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        // Fetch devotional data from Firestore
        db.collection("devotional").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DevotionalModel devotional = documentSnapshot.toObject(DevotionalModel.class);
                        if (devotional != null) {
                            devotional.setId(id);
                            updateUIWithDevotional(devotional);
                            saveDevotionalToLocal(devotional);

                            // Check if reflection is already submitted
                            checkReflectionStatus(controlId, email, id); // Check if reflection exists for this devotional

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
    private void checkReflectionStatus(String controlId, String email, String id) {
        db.collection("kidsReflection")
                .whereEqualTo("controlId", controlId)
                .whereEqualTo("email", email)
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Disable the EditText and Submit button if reflection already exists
                        answerreflection.setEnabled(false); // Disable the EditText
                        submit.setEnabled(false); // Disable the Submit button
                        submit.setBackgroundColor(getResources().getColor(R.color.gray)); // Change button color to gray

                        // Fetch the existing reflection from Firestore
                        String existingReflection = querySnapshot.getDocuments().get(0).getString("reflectionanswer");

                        // Set the existing reflection in the EditText
                        answerreflection.setText(existingReflection);
                    }
                    else {
                        // Enable reflection input if not yet submitted
                        answerreflection.setEnabled(true);
                        submit.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking reflection status", e);
                });
    }



    private void checkAndSubmitReflection(String id, String reflectionanswer, String email, String controlId) {
        // Step 1: Check if reflection already exists for the given devotionalId and controlId
        Map<String, Object> reflectionData = new HashMap<>();
        reflectionData.put("id", id);
        reflectionData.put("controlId", controlId);
        reflectionData.put("reflectionanswer", reflectionanswer);
        reflectionData.put("email", email);
        reflectionData.put("timestamp", new Timestamp(new Date()));

        db.collection("kidsReflection")
                .whereEqualTo("id", id)
                .whereEqualTo("controlId", controlId)
                .whereEqualTo("email", email) // Validate email as well
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Step 2: If no existing reflection, proceed with submission
                        submitReflection(id, reflectionanswer, email, controlId);
                    } else {
                        // Step 3: Reflection already submitted, disable button and show message
                        handleExistingReflection(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking reflection existence", e);
                    Toast.makeText(DevotionalKids.this, "Failed to check reflection status.", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleExistingReflection(QuerySnapshot querySnapshot) {
        // Handle existing reflection (disable button, show existing reflection, etc.)
        Toast.makeText(DevotionalKids.this, "You have already submitted your reflection.", Toast.LENGTH_SHORT).show();
        submit.setEnabled(false);
        submit.setText("Submitted");
        submit.setTextColor(getResources().getColor(R.color.white));
        submit.setBackground(getResources().getDrawable(R.drawable.shadow3dbutton));
        submit.setBackgroundColor(getResources().getColor(R.color.gray));  // Gray out button
        answerreflection.setEnabled(false); // Disable further input

        // Safely retrieve the existing reflection
        String existingReflection = querySnapshot.getDocuments().get(0).getString("reflectionanswer");
        if (existingReflection != null) {
            answerreflection.setText(existingReflection); // Display the existing reflection
        }
        Toast.makeText(DevotionalKids.this, "Reflection already submitted.", Toast.LENGTH_SHORT).show();
    }

    private void submitReflection(String devotionalId, String reflectionText, String email, String controlId) {
        // Step 1: Create a new reflection object
        Map<String, Object> reflectionData = new HashMap<>();
        DevotionalModel reflection = new DevotionalModel();
        reflection.setId(devotionalId);
        reflection.setReflectionanswer(reflectionText);
        reflection.setTimestamp(new Timestamp(new Date())); // Capture timestamp
        reflection.setEmail(email);
        reflection.setControlId(controlId);

        // Step 2: Inflate custom confirmation dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.areyousure, null);

        // Initialize the dialog with the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(DevotionalKids.this);
        builder.setView(dialogView);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Get references to the buttons
        Button confirmButton = dialogView.findViewById(R.id.confirm);
        Button cancelButton = dialogView.findViewById(R.id.cancel);

        // Set the behavior for the "Confirm" button
        confirmButton.setOnClickListener(v -> {
            // If confirmed, submit reflection to Firestore
            db.collection("kidsReflection")
                    .add(reflection)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(DevotionalKids.this, "Reflection submitted successfully!", Toast.LENGTH_SHORT).show();
                        // Clear input field and update UI
                        answerreflection.setText("");
                        disableSubmitButton();
                        answerreflection.setEnabled(false); // Disable the EditText after submission
                        answerreflection.setText(reflectionText); // Display the submitted reflection
                        showSubmissionSuccessDialog();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DevotionalKids", "Error submitting reflection", e);
                        Toast.makeText(DevotionalKids.this, "Failed to submit reflection. Please try again.", Toast.LENGTH_SHORT).show();
                    });
            dialog.dismiss();  // Close the dialog
        });

        // Set the behavior for the "Cancel" button
        cancelButton.setOnClickListener(v -> {
            // If canceled, do nothing
            dialog.dismiss();
        });

        // Show the dialog
        dialog.setCancelable(false); // Prevent dialog from being dismissed by tapping outside
        dialog.show();
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

        new Handler().postDelayed(() -> {
            successDialog.dismiss();  // Dismiss the success dialog after 2 seconds
            showSuccessDialog();  // Show the next success dialog
        }, 2000);  // Dismiss after 2 seconds
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DevotionalKids.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reward_devotion, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageView imageView = dialogView.findViewById(R.id.gifhands);
        Glide.with(this).load(R.raw.prayersign).into(imageView);


        AppCompatButton buttonOk = dialogView.findViewById(R.id.continuebutton);
        buttonOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
