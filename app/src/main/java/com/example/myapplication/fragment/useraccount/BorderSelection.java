package com.example.myapplication.fragment.useraccount;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.FragmentSettings;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class BorderSelection extends AppCompatActivity {

    private int selectedBorderResourceId = -1;
    private CircleImageView previouslySelectedAvatar = null;
    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.border_pick);

        MaterialButton changeButton = findViewById(R.id.buttonchange);
        changeButton.setEnabled(false); // Disable the "Change" button initially

        // Initialize database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Load the current user details
        loadCurrentUser();

        // Set click listeners for each avatar
        CircleImageView bronzeBorder = findViewById(R.id.bronze);
        CircleImageView silverBorder = findViewById(R.id.silver);
        CircleImageView angelblueBorder = findViewById(R.id.angelblue);
        CircleImageView angelpinkBorder = findViewById(R.id.angelpink);
        CircleImageView angelrainbowBorder = findViewById(R.id.angelrainbow);
        CircleImageView angelskyblueBorder = findViewById(R.id.angelskyblue);
        CircleImageView exclusiveBorder = findViewById(R.id.exclusive);


        // Set click listeners for each avatar
        bronzeBorder.setOnClickListener(v -> selectAvatar(bronzeBorder, R.drawable.bronze, changeButton));
        silverBorder.setOnClickListener(v -> selectAvatar(silverBorder, R.drawable.silver, changeButton));
        angelblueBorder.setOnClickListener(v -> selectAvatar(angelblueBorder, R.drawable.angelblue, changeButton));
        angelpinkBorder.setOnClickListener(v -> selectAvatar(angelpinkBorder, R.drawable.angelpink, changeButton));
        angelrainbowBorder.setOnClickListener(v -> selectAvatar(angelrainbowBorder, R.drawable.angelrainbow, changeButton));
        angelskyblueBorder.setOnClickListener(v -> selectAvatar(angelskyblueBorder, R.drawable.angelskyblue, changeButton));
        exclusiveBorder.setOnClickListener(v -> selectAvatar(exclusiveBorder, R.drawable.exclusive, changeButton)); // Added

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
