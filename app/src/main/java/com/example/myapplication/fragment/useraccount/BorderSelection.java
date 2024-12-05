package com.example.myapplication.fragment.useraccount;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.achievement.GameAchievementDao;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.FragmentSettings;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BorderSelection extends AppCompatActivity {

    private int selectedBorderResourceId = -1;
    private CircleImageView previouslySelectedAvatar = null;
    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;
    private StoryAchievementDao storyAchievementDao;
    private GameAchievementDao gameAchievementDao;

    private static final Map<String, Integer> BORDER_REQUIREMENTS = new HashMap<String, Integer>() {{
        put("covenant", 0);  // Default border - always unlocked
        put("shekinah", 10);  // Requires 2 achievements
        put("zion", 20);
        put("elohim", 30);
        put("promise", 40);
        put("celestine", 50);
        put("eden", 60);
        put("christmas", 70); // Requires all achievements
    }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.border_pick);

        MaterialButton changeButton = findViewById(R.id.buttonchange);
        changeButton.setEnabled(false); // Disable the "Change" button initially

        // Initialize database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        setupViews();
        initializeDatabase();
        updateBorderLockStatus();
        // Load the current user details
        loadCurrentUser();
        ImageButton about = findViewById(R.id.about);
        about.setOnClickListener(v -> {
            // Play sound effect
            showAboutDialog();
        });

        // Set click listeners for each avatar
        CircleImageView bronzeBorder = findViewById(R.id.bronze);
        CircleImageView silverBorder = findViewById(R.id.silver);
        CircleImageView angelblueBorder = findViewById(R.id.angelblue);
        CircleImageView angelpinkBorder = findViewById(R.id.angelpink);
        CircleImageView angelrainbowBorder = findViewById(R.id.angelrainbow);
        CircleImageView angelskyblueBorder = findViewById(R.id.angelskyblue);
        CircleImageView angelgoldlueBorder = findViewById(R.id.angelgold);
        CircleImageView exclusiveBorder = findViewById(R.id.exclusive);


        // Set click listeners for each avatar
        bronzeBorder.setOnClickListener(v -> selectAvatar(bronzeBorder, R.drawable.bronze, changeButton));
        silverBorder.setOnClickListener(v -> selectAvatar(silverBorder, R.drawable.silver, changeButton));
        angelblueBorder.setOnClickListener(v -> selectAvatar(angelblueBorder, R.drawable.angelblue, changeButton));
        angelpinkBorder.setOnClickListener(v -> selectAvatar(angelpinkBorder, R.drawable.angelpink, changeButton));
        angelrainbowBorder.setOnClickListener(v -> selectAvatar(angelrainbowBorder, R.drawable.angelrainbow, changeButton));
        angelskyblueBorder.setOnClickListener(v -> selectAvatar(angelskyblueBorder, R.drawable.angelskyblue, changeButton));
        angelgoldlueBorder.setOnClickListener(v -> selectAvatar(angelgoldlueBorder, R.drawable.angelgold, changeButton));
        exclusiveBorder.setOnClickListener(v -> selectAvatar(exclusiveBorder, R.drawable.exclusive, changeButton)); // Added

        // Handle cancel button click
        // Handle cancel button click
        MaterialButton cancelButton = findViewById(R.id.buttoncancel);
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // No avatar selected, cancel
            finish();
        });

        // Handle change button click
        changeButton.setOnClickListener(v -> {
            if (selectedBorderResourceId != -1 && currentUser != null) {
                updateAvatarInDatabase();
            } else {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAboutDialog() {
        // Create data objects to hold border information
        class BorderInfo {
            String name;
            String description;
            String requirement;
            int imageResource;

            BorderInfo(String name, String description, String requirement, int imageResource) {
                this.name = name;
                this.description = description;
                this.requirement = requirement;
                this.imageResource = imageResource;
            }
        }

        BorderInfo[] borderInfos = {
                new BorderInfo(
                        "Covenant Border",
                        "The starting border representing the beginning of your journey, just as God established His first covenant with humanity. Available to all users as they begin their spiritual journey.",
                        "0 achievements - Starting border",
                        R.drawable.bronze
                ),
                new BorderInfo(
                        "Shekinah Border",
                        "Named after the divine presence of God. This border symbolizes the first steps of commitment in your spiritual journey.",
                        "2 achievements - Represents taking initial steps in faith",
                        R.drawable.silver
                ),
                new BorderInfo(
                        "Zion Border",
                        "Represents the holy city and dwelling place of God. The blue color symbolizes heavenly wisdom and divine guidance.",
                        "3 achievements - Shows growing spiritual understanding",
                        R.drawable.angelblue
                ),
                new BorderInfo(
                        "Elohim Border",
                        "Named after one of God's names meaning Creator. The pink hue represents God's tender love and care for His creation.",
                        "4 achievements - Indicates deepening spiritual awareness",
                        R.drawable.angelpink
                ),
                new BorderInfo(
                        "Promise Border",
                        "Inspired by God's rainbow covenant with Noah. The rainbow colors symbolize God's faithfulness to His promises.",
                        "6 achievements - Marks significant spiritual progress",
                        R.drawable.angelrainbow
                ),
                new BorderInfo(
                        "Celestine Border",
                        "Represents the celestial realm and heavenly places. The sky blue color symbolizes the expanse of God's kingdom.",
                        "8 achievements - Shows advanced spiritual growth",
                        R.drawable.angelskyblue
                ),
                new BorderInfo(
                        "Eden Border",
                        "Named after the perfect garden where God walked with humanity. Gold represents divine perfection and glory.",
                        "10 achievements - Symbolizes exceptional spiritual dedication",
                        R.drawable.angelgold
                ),
                new BorderInfo(
                        "Christmas Border",
                        "A special border celebrating the birth of Christ. Reserved for those who have completed all achievements, representing the ultimate gift.",
                        "12 achievements - Ultimate accomplishment showing complete dedication",
                        R.drawable.exclusive
                )
        };

        final int[] currentIndex = {0};
        int[] dotIndicators = {R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4, R.id.dot5, R.id.dot6, R.id.dot7,R.id.dot8};

        View dialogView = LayoutInflater.from(this).inflate(R.layout.aboutonreward, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        ImageView instructionImage = dialogView.findViewById(R.id.instruction);
        TextView instructionText = dialogView.findViewById(R.id.textinstruction);
        TextView titleText = dialogView.findViewById(R.id.name);
        TextView requirementText = dialogView.findViewById(R.id.textrequired);
        ImageButton nextButton = dialogView.findViewById(R.id.next);
        ImageButton previousButton = dialogView.findViewById(R.id.previous);
        AppCompatButton playButton = dialogView.findViewById(R.id.play);

        // Initially disable the "Okay" button
        playButton.setEnabled(false);
        playButton.setAlpha(0.5f);

        // Function to update all content
        Runnable updateAllContent = () -> {
            BorderInfo currentInfo = borderInfos[currentIndex[0]];

            // Apply slide animation to all views
            Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);

            instructionImage.startAnimation(slideIn);
            instructionText.startAnimation(slideIn);
            titleText.startAnimation(slideIn);
            requirementText.startAnimation(slideIn);

            instructionImage.setImageResource(currentInfo.imageResource);
            titleText.setText(currentInfo.name);
            instructionText.setText(currentInfo.description);
            requirementText.setText(currentInfo.requirement);

            updateDots(dotIndicators, currentIndex[0], dialogView);

            // Update button states
            previousButton.setEnabled(currentIndex[0] > 0);
            previousButton.setAlpha(currentIndex[0] > 0 ? 1f : 0.5f);

            nextButton.setEnabled(currentIndex[0] < borderInfos.length - 1);
            nextButton.setAlpha(currentIndex[0] < borderInfos.length - 1 ? 1f : 0.5f);

            // Enable "Okay" button only on the last slide
            playButton.setEnabled(currentIndex[0] == borderInfos.length - 1);
            playButton.setAlpha(currentIndex[0] == borderInfos.length - 1 ? 1f : 0.5f);
        };

        // Initial content update
        updateAllContent.run();

        AlertDialog dialog = builder.create();
        dialog.show();

        nextButton.setOnClickListener(v -> {
            if (currentIndex[0] < borderInfos.length - 1) {
                currentIndex[0]++;
                updateAllContent.run();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateAllContent.run();
            }
        });

        playButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Enable the main "Play" button if it exists
            View playView = findViewById(R.id.play);
            if (playView != null) {
                playView.setEnabled(true);
                playView.setAlpha(1f);
            }
        });
    }
    private void updateDots(int[] dotIndicators, int currentIndex, View dialogView) {
        for (int i = 0; i < dotIndicators.length; i++) {
            ImageView dot = dialogView.findViewById(dotIndicators[i]);
            if (i == currentIndex) {
                dot.setImageResource(R.drawable.dot_active); // Active dot drawable
            } else {
                dot.setImageResource(R.drawable.dot_inactive); // Inactive dot drawable
            }
        }
    }

    private void initializeDatabase() {
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        storyAchievementDao = db.storyAchievementDao();
        gameAchievementDao = db.gameAchievementDao();
    }
    private void setupViews() {
        MaterialButton changeButton = findViewById(R.id.buttonchange);
        changeButton.setEnabled(false);

        // Map of border views and their IDs
        Map<String, CircleImageView> borderViews = new HashMap<>();
        borderViews.put("covenant", findViewById(R.id.bronze));
        borderViews.put("shekinah", findViewById(R.id.silver));
        borderViews.put("zion", findViewById(R.id.angelblue));
        borderViews.put("elohim", findViewById(R.id.angelpink));
        borderViews.put("promise", findViewById(R.id.angelrainbow));
        borderViews.put("celestine", findViewById(R.id.angelskyblue));
        borderViews.put("eden", findViewById(R.id.angelgold));
        borderViews.put("christmas", findViewById(R.id.exclusive));

        // Set up click listeners for each border
        for (Map.Entry<String, CircleImageView> entry : borderViews.entrySet()) {
            String borderName = entry.getKey();
            CircleImageView borderView = entry.getValue();
            int resourceId = getResources().getIdentifier(borderName, "drawable", getPackageName());

            if (isBorderUnlocked(borderName)) {
                borderView.setEnabled(true);
                borderView.setAlpha(1.0f);
                borderView.setOnClickListener(v -> selectAvatar(borderView, resourceId, changeButton));
            } else {
                borderView.setEnabled(false);
                borderView.setAlpha(0.5f);
                borderView.setOnClickListener(null); // Remove click listener for locked borders
            }
        }

        // Cancel button setup
        MaterialButton cancelButton = findViewById(R.id.buttoncancel);
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Change button setup
        changeButton.setOnClickListener(v -> {
            if (selectedBorderResourceId != -1 && currentUser != null) {
                updateAvatarInDatabase();
            } else {
                Toast.makeText(this, "Please select a border", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateBorderLockStatus() {
        AsyncTask.execute(() -> {
            int totalAchievements = getTotalCompletedAchievements();

            runOnUiThread(() -> {
                for (Map.Entry<String, Integer> entry : BORDER_REQUIREMENTS.entrySet()) {
                    String borderName = entry.getKey();
                    int requirement = entry.getValue();
                    CircleImageView borderView = findViewById(getResources().getIdentifier(
                            borderNameToViewId(borderName), "id", getPackageName()));

                    if (totalAchievements < requirement) {
                        // Add lock overlay and disable the view
                        addLockOverlay(borderView);
                        borderView.setEnabled(false); // Disable the view
                        borderView.setAlpha(0.5f); // Make it appear disabled
                    } else {
                        borderView.setEnabled(true);
                        borderView.setAlpha(1.0f);
                    }
                }
            });
        });
    }
    private void addLockOverlay(CircleImageView borderView) {
        // Create an overlay with a lock icon
        ImageView lockOverlay = new ImageView(this);
        lockOverlay.setImageResource(R.drawable.lockborder);

        // Add the overlay to the parent layout
        ViewGroup parent = (ViewGroup) borderView.getParent();
        FrameLayout container = new FrameLayout(this);

        // Replace the CircleImageView with the container
        int index = parent.indexOfChild(borderView);
        parent.removeView(borderView);
        parent.addView(container, index, borderView.getLayoutParams());

        // Add both views to the container
        container.addView(borderView);
        container.addView(lockOverlay);

        // Center the lock icon
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        lockOverlay.setLayoutParams(params);

        // Make the container non-clickable for locked borders
        container.setClickable(false);
        container.setEnabled(false);
    }


    private int getTotalCompletedAchievements() {
        int storyAchievements = 0;
        int gameAchievements = 0;

        if (storyAchievementDao != null) {
            storyAchievements = storyAchievementDao.getCompletedAchievementsCount();
        }

        if (gameAchievementDao != null) {
            gameAchievements = gameAchievementDao.getCompletedAchievementsCount();
        }

        return storyAchievements + gameAchievements;
    }

    private boolean isBorderUnlocked(String borderName) {
        int required = BORDER_REQUIREMENTS.getOrDefault(borderName, 0);
        return getTotalCompletedAchievements() >= required;
    }

    private void showUnlockRequirements(String borderName) {
        int required = BORDER_REQUIREMENTS.get(borderName);
        int current = getTotalCompletedAchievements();

        String message = String.format(
                "This border requires %d achievements to unlock. You currently have %d achievements.",
                required, current
        );

        new AlertDialog.Builder(this)
                .setTitle("Border Locked")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // Helper method to convert border names to view IDs
    private String borderNameToViewId(String borderName) {
        switch (borderName) {
            case "covenant": return "bronze";
            case "shekinah": return "silver";
            case "zion": return "angelblue";
            case "elohim": return "angelpink";
            case "promise": return "angelrainbow";
            case "celestine": return "angelskyblue";
            case "eden": return "angelgold";
            case "christmas": return "exclusive";
            default: return borderName;
        }
    }

    private void selectAvatar(CircleImageView avatarImageView, int borderResourceId, MaterialButton changeButton) {
        // Deselect previously selected avatar if any
        if (previouslySelectedAvatar != null) {
            previouslySelectedAvatar.setBorderWidth(0); // Remove border from previously selected avatar
        }

        // Highlight the selected avatar
        avatarImageView.setBorderWidth(10); // Add a border around the selected avatar
        avatarImageView.setBorderColor(android.graphics.Color.GREEN); // Use green color for the border

        previouslySelectedAvatar = avatarImageView; // Store the selected avatar
        selectedBorderResourceId = borderResourceId; // Store the resource ID
        changeButton.setEnabled(true); // Enable the change button
    }

    private void loadCurrentUser() {
        AsyncTask.execute(() -> {
            // Assuming the current logged-in user is fetched, replace this with actual logic
            currentUser = userDao.getFirstUser();

            if (currentUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "User loaded successfully", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateAvatarInDatabase() {
        AsyncTask.execute(() -> {
            if (currentUser != null) {
                // Convert the selected avatar drawable to a byte array
                Drawable drawable = getResources().getDrawable(selectedBorderResourceId);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                byte[] avatarImage = bitmapToByteArray(bitmap);

                // Update user details
                currentUser.setBorderName(getResources().getResourceEntryName(selectedBorderResourceId));
                currentUser.setBorderResourceId(selectedBorderResourceId);
                currentUser.setBorderImage(avatarImage);

                // Update the user in the database
                userDao.updateUser(currentUser);

                // Redirect to FragmentSettings with transition
                runOnUiThread(() -> {
                    Toast.makeText(BorderSelection.this, "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BorderSelection.this, FragmentSettings.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim); // Apply animations
                    finish();
                });
            }
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
