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
import com.google.firebase.firestore.DocumentSnapshot;
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

    TextView memoryverse, verse, feedbackTextView, feedbacktitle;
    AppCompatButton submit;
    EditText answerreflection;
    ImageView devotionalThumbnail,arrowback;
    SwipeRefreshLayout swipeRefreshLayout;
    private String feedback = "no feedback";
    private String badge = "no badge";
    private FirebaseFirestore db;
    private String devotionalId;
    private AppDatabase appDatabase;
    private TextToSpeech textToSpeech;
    private AppCompatButton speechToTextButton;
    private AppCompatButton readButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kidsdevotional);
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);
        arrowback = findViewById(R.id.arrowback);
        arrowback.setOnClickListener(v -> onBackPressed());
        memoryverse = findViewById(R.id.memoryverse);
        verse = findViewById(R.id.verse);
        devotionalThumbnail = findViewById(R.id.devotionalThumbnail);
        answerreflection = findViewById(R.id.answerreflection);
        speechToTextButton = findViewById(R.id.speechttext);
        submit = findViewById(R.id.submit);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        feedbackTextView = findViewById(R.id.feedback);
        feedbacktitle = findViewById(R.id.feedbacktitle);
        readButton = findViewById(R.id.read);
        readButton.setOnClickListener(v -> speakMemoryVerse());
        devotionalId = getIntent().getStringExtra("devotionalId");
        String controlId = getIntent().getStringExtra("controlId");
        String email = getIntent().getStringExtra("email");
        if (devotionalId == null) {
            fetchTodayDevotional();
        } else {
            loadDevotional(devotionalId, controlId, email);
        }
        if (email != null && controlId != null) {
            checkAndShowBadge(email, controlId);
        }
        updateCardColors();
        speechToTextButton.setOnClickListener(v -> startSpeechToText());
        swipeRefreshLayout.setOnRefreshListener(() -> loadDevotional(devotionalId, controlId, email));
        submit.setOnClickListener(v -> {
            String reflection = answerreflection.getText().toString().trim();
            if (!reflection.isEmpty()) {

                if (email != null && controlId != null) {
                    checkAndSubmitReflection(devotionalId, reflection, email, badge, feedback, controlId);
                } else {
                    Toast.makeText(DevotionalKids.this, "Email or Control ID is missing.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DevotionalKids.this, "Please enter your reflection.", Toast.LENGTH_SHORT).show();
            }
        });
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
    private void speakMemoryVerse() {
        String verseText = memoryverse.getText().toString();
        if (!verseText.isEmpty()) {
            textToSpeech.setSpeechRate(0.7f);
            textToSpeech.setPitch(1.0f);
            textToSpeech.speak(verseText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    private void updateCardColors() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        resetCardViews();
        int selectedColor = getResources().getColor(R.color.blue);
        int selectedTextColor = getResources().getColor(R.color.white);
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
        ((CardView) findViewById(R.id.mon)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.tue)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.wed)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.thu)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.fri)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.sat)).setCardBackgroundColor(getResources().getColor(R.color.white));
        ((CardView) findViewById(R.id.sun)).setCardBackgroundColor(getResources().getColor(R.color.white));
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
        TextView textView = (TextView) cardView.getChildAt(0);
        textView.setTextColor(textColor);
    }

    private void setTextColor(int cardId, int textColor) {
        CardView cardView = findViewById(cardId);
        TextView textView = (TextView) cardView.getChildAt(0);
        textView.setTextColor(textColor);
    }
    private void startSpeechToText() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
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
    private void fetchTodayDevotional() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();
        db.collection("devotional")
                .whereGreaterThanOrEqualTo("timestamp", todayStart)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1)
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

    private void checkAndShowBadge(String email, String controlId) {
        db.collection("kidsReflection")
                .whereEqualTo("email", email)
                .whereEqualTo("controlId", controlId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot mostRecent = null;
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            if (mostRecent == null ||
                                    doc.getTimestamp("timestamp").compareTo(mostRecent.getTimestamp("timestamp")) > 0) {
                                mostRecent = doc;
                            }
                        }
                        if (mostRecent != null) {
                            String badge = mostRecent.getString("badge");
                            if (badge != null && !badge.isEmpty() && !"no badge".equals(badge)) {
                                showBadgePopup(badge);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking badge", e);
                    Toast.makeText(DevotionalKids.this,
                            "Failed to check badge status", Toast.LENGTH_SHORT).show();
                });
    }
    private void showBadgePopup(String badge) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.badge, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            ImageView badgeImageView = dialogView.findViewById(R.id.badge);
            TextView badgeTextView = dialogView.findViewById(R.id.badgeTextView);
            switch (badge) {
                case "Star Thinker":
                    badgeImageView.setImageResource(R.drawable.startthinker);
                    badgeTextView.setText("Star Thinker");
                    break;
                case "Creative Contributor":
                    badgeImageView.setImageResource(R.drawable.creativecontributiion);
                    badgeTextView.setText("Creative Contributor");
                    break;
                case "Consistent Reflector":
                    badgeImageView.setImageResource(R.drawable.consistentreflector);
                    badgeTextView.setText("Consistent Reflector");
                    break;
                default:
                    dialog.dismiss();
                    return;
            }
            AppCompatButton closeButton = dialogView.findViewById(R.id.button_done);
            closeButton.setOnClickListener(v -> dialog.dismiss());
            if (!isFinishing()) {
                dialog.show();
            }
        });
    }
    private void loadDevotional(String id, String controlId, String email) {
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
                            checkReflectionStatus(controlId, email, id);
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
    private void updateUIWithDevotional(DevotionalModel devotional) {
        if (devotional == null) return;
        memoryverse.setText(devotional.getMemoryverse());
        verse.setText(devotional.getVerse());
        if (devotional.getFeedback() != null && !devotional.getFeedback().isEmpty()) {
            feedbackTextView.setText(devotional.getFeedback());
            feedbackTextView.setVisibility(View.VISIBLE);
            feedbacktitle.setVisibility(View.VISIBLE);
        } else {
            feedbackTextView.setVisibility(View.GONE);
            feedbacktitle.setVisibility(View.GONE);
        }
        if (devotional.getImageUrl() != null && !devotional.getImageUrl().isEmpty()) {
            Glide.with(DevotionalKids.this)
                    .load(devotional.getImageUrl())
                    .placeholder(R.drawable.image)
                    .into(devotionalThumbnail);
        } else {
            devotionalThumbnail.setImageResource(R.drawable.image);
        }
    }
    private void saveDevotionalToLocal(DevotionalModel devotional) {
        Executors.newSingleThreadExecutor().execute(() -> appDatabase.devotionalDao().insert(devotional));
    }
    private void checkReflectionStatus(String controlId, String email, String id) {
        db.collection("kidsReflection")
                .whereEqualTo("controlId", controlId)
                .whereEqualTo("email", email)
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        answerreflection.setEnabled(false);
                        submit.setEnabled(false);
                        submit.setBackgroundColor(getResources().getColor(R.color.gray));
                        String existingReflection = querySnapshot.getDocuments().get(0).getString("reflectionanswer");
                        String existingFeedback = querySnapshot.getDocuments().get(0).getString("feedback");
                        answerreflection.setText(existingReflection);
                        if (existingFeedback != null && !existingFeedback.isEmpty()) {
                            feedbackTextView.setText(existingFeedback);
                            feedbackTextView.setVisibility(View.VISIBLE);
                            feedbacktitle.setVisibility(View.VISIBLE);
                        } else {
                            feedbackTextView.setVisibility(View.GONE);
                            feedbacktitle.setVisibility(View.GONE);
                        }
                    }
                    else {
                        answerreflection.setEnabled(true);
                        submit.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking reflection status", e);
                });
    }
    private void checkAndSubmitReflection(String id, String reflectionanswer, String email,String badge, String feedback,String controlId) {
        Map<String, Object> reflectionData = new HashMap<>();
        reflectionData.put("id", id);
        reflectionData.put("controlId", controlId);
        reflectionData.put("reflectionanswer", reflectionanswer);
        reflectionData.put("email", email);
        reflectionData.put("badge", badge);
        reflectionData.put("feedback", feedback);
        reflectionData.put("timestamp", new Timestamp(new Date()));
        db.collection("kidsReflection")
                .whereEqualTo("id", id)
                .whereEqualTo("controlId", controlId)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        submitReflection(id, reflectionanswer, email, badge, feedback,controlId);
                    } else {
                        handleExistingReflection(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DevotionalKids", "Error checking reflection existence", e);
                    Toast.makeText(DevotionalKids.this, "Failed to check reflection status.", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleExistingReflection(QuerySnapshot querySnapshot) {
        Toast.makeText(DevotionalKids.this, "You have already submitted your reflection.", Toast.LENGTH_SHORT).show();
        submit.setEnabled(false);
        submit.setText("Submitted");
        submit.setTextColor(getResources().getColor(R.color.white));
        submit.setBackground(getResources().getDrawable(R.drawable.shadow3dbutton));
        submit.setBackgroundColor(getResources().getColor(R.color.gray));  // Gray out button
        answerreflection.setEnabled(false);
        String existingReflection = querySnapshot.getDocuments().get(0).getString("reflectionanswer");
        if (existingReflection != null) {
            answerreflection.setText(existingReflection);
        }
        Toast.makeText(DevotionalKids.this, "Reflection already submitted.", Toast.LENGTH_SHORT).show();
    }
    private void submitReflection(String devotionalId, String reflectionText, String email, String badge, String feedback,String controlId) {
        Map<String, Object> reflectionData = new HashMap<>();
        DevotionalModel reflection = new DevotionalModel();
        reflection.setId(devotionalId);
        reflection.setReflectionanswer(reflectionText);
        reflection.setTimestamp(new Timestamp(new Date()));
        reflection.setEmail(email);
        reflection.setBadge(badge);
        reflection.setFeedback(feedback);
        reflection.setControlId(controlId);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.areyousure, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(DevotionalKids.this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        Button confirmButton = dialogView.findViewById(R.id.confirm);
        Button cancelButton = dialogView.findViewById(R.id.cancel);
        confirmButton.setOnClickListener(v -> {
            db.collection("kidsReflection")
                    .add(reflection)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(DevotionalKids.this, "Reflection submitted successfully!", Toast.LENGTH_SHORT).show();
                        answerreflection.setText("");
                        disableSubmitButton();
                        answerreflection.setEnabled(false);
                        answerreflection.setText(reflectionText);
                        showSubmissionSuccessDialog();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DevotionalKids", "Error submitting reflection", e);
                        Toast.makeText(DevotionalKids.this, "Failed to submit reflection. Please try again.", Toast.LENGTH_SHORT).show();
                    });
            dialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.setCancelable(false);
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
        builder.setCancelable(false);
        AlertDialog successDialog = builder.create();
        successDialog.show();
        new Handler().postDelayed(() -> {
            successDialog.dismiss();
            showSuccessDialog();
        }, 2000);
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
