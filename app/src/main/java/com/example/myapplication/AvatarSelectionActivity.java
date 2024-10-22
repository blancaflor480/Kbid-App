package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.FragmentSettings;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarSelectionActivity extends AppCompatActivity {

    private int selectedAvatarResourceId = -1;
    private CircleImageView previouslySelectedAvatar = null;
    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar_editprofile);

        MaterialButton changeButton = findViewById(R.id.buttonchange);
        changeButton.setEnabled(false); // Disable the "Change" button initially

        // Initialize database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Load the current user details
        loadCurrentUser();

        // Set click listeners for each avatar
        CircleImageView bunnyAvatar = findViewById(R.id.bunny);
        CircleImageView boarAvatar = findViewById(R.id.boar);
        CircleImageView crocsAvatar = findViewById(R.id.crocs);
        CircleImageView horseAvatar = findViewById(R.id.horse);
        CircleImageView noahAvatar = findViewById(R.id.noah);
        CircleImageView lionAvatar = findViewById(R.id.lion);
        CircleImageView mooseAvatar = findViewById(R.id.moose);
        CircleImageView koalaAvatar = findViewById(R.id.koala);
        CircleImageView penguinAvatar = findViewById(R.id.penguin);
        CircleImageView tigerAvatar = findViewById(R.id.tiger);

        // Set click listeners for each avatar
        bunnyAvatar.setOnClickListener(v -> selectAvatar(bunnyAvatar, R.drawable.bunny, changeButton));
        boarAvatar.setOnClickListener(v -> selectAvatar(boarAvatar, R.drawable.boar, changeButton));
        crocsAvatar.setOnClickListener(v -> selectAvatar(crocsAvatar, R.drawable.crocs, changeButton));
        horseAvatar.setOnClickListener(v -> selectAvatar(horseAvatar, R.drawable.horse, changeButton));
        noahAvatar.setOnClickListener(v -> selectAvatar(noahAvatar, R.drawable.noah, changeButton));
        lionAvatar.setOnClickListener(v -> selectAvatar(lionAvatar, R.drawable.lion, changeButton));
        mooseAvatar.setOnClickListener(v -> selectAvatar(mooseAvatar, R.drawable.moose, changeButton)); // Added
        koalaAvatar.setOnClickListener(v -> selectAvatar(koalaAvatar, R.drawable.koala, changeButton)); // Added
        penguinAvatar.setOnClickListener(v -> selectAvatar(penguinAvatar, R.drawable.penguin, changeButton)); // Added
        tigerAvatar.setOnClickListener(v -> selectAvatar(tigerAvatar, R.drawable.tiger, changeButton)); // Added

        // Handle cancel button click
        MaterialButton cancelButton = findViewById(R.id.buttoncancel);
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // No avatar selected, cancel
            finish();
        });

        // Handle change button click
        changeButton.setOnClickListener(v -> {
            if (selectedAvatarResourceId != -1 && currentUser != null) {
                updateAvatarInDatabase();
            } else {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void selectAvatar(CircleImageView avatarImageView, int avatarResourceId, MaterialButton changeButton) {
        // Deselect previously selected avatar if any
        if (previouslySelectedAvatar != null) {
            previouslySelectedAvatar.setBorderWidth(0); // Remove border from previously selected avatar
        }

        // Highlight the selected avatar
        avatarImageView.setBorderWidth(10); // Add a border around the selected avatar
        avatarImageView.setBorderColor(android.graphics.Color.GREEN); // Use green color for the border

        previouslySelectedAvatar = avatarImageView; // Store the selected avatar
        selectedAvatarResourceId = avatarResourceId; // Store the resource ID
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
                Drawable drawable = getResources().getDrawable(selectedAvatarResourceId);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                byte[] avatarImage = bitmapToByteArray(bitmap);

                // Update user details
                currentUser.setAvatarName(getResources().getResourceEntryName(selectedAvatarResourceId));
                currentUser.setAvatarResourceId(selectedAvatarResourceId);
                currentUser.setAvatarImage(avatarImage);

                // Update the user in the database
                userDao.updateUser(currentUser);

                // Redirect to FragmentSettings with transition
                runOnUiThread(() -> {
                    Toast.makeText(AvatarSelectionActivity.this, "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AvatarSelectionActivity.this, FragmentSettings.class);
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
