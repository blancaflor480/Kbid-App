package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarActivity extends AppCompatActivity {

    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar_user);

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Load the current user and setup avatar listeners after loading is complete
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        AsyncTask.execute(() -> {
            currentUser = userDao.getFirstUser(); // Assuming you only have one user and it's the first one in the database

            // After loading the user, proceed to set up the click listeners
            runOnUiThread(this::setupAvatarClickListeners);
        });
    }

    private void setupAvatarClickListeners() {
        // Initialize the CircleImageView components for avatars
        CircleImageView[] avatars = {
                findViewById(R.id.bunny),
                findViewById(R.id.boar),
                findViewById(R.id.crocs),
                findViewById(R.id.horse),
                findViewById(R.id.noah),
                findViewById(R.id.lion),
                findViewById(R.id.moose),
                findViewById(R.id.koala),
                findViewById(R.id.penguin),
                findViewById(R.id.tiger)
        };

        // Resource IDs for the corresponding avatars
        int[] avatarResourceIds = {
                R.drawable.bunny,
                R.drawable.boar,
                R.drawable.crocs,
                R.drawable.horse,
                R.drawable.noah,
                R.drawable.lion,
                R.drawable.moose,
                R.drawable.koala,
                R.drawable.penguin,
                R.drawable.tiger
        };

        // Set click listeners for each avatar
        for (int i = 0; i < avatars.length; i++) {
            final String avatarName = getResources().getResourceEntryName(avatarResourceIds[i]); // Get the name from the resource ID
            final int resourceId = avatarResourceIds[i];
            avatars[i].setOnClickListener(view -> selectAvatar(avatarName, resourceId));
        }
    }

    private void selectAvatar(String avatarName, int avatarResourceId) {
        // Convert drawable to byte[]
        Drawable drawable = getResources().getDrawable(avatarResourceId);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        byte[] avatarImage = bitmapToByteArray(bitmap);

        // Update the user with the selected avatar
        AsyncTask.execute(() -> {
            if (currentUser != null) {
                currentUser.setAvatarName(avatarName);
                currentUser.setAvatarResourceId(avatarResourceId);
                currentUser.setAvatarImage(avatarImage); // Save the image as byte[]

                userDao.updateUser(currentUser);

                // Redirect to HomeActivity after saving
                runOnUiThread(() -> {
                    Intent intent = new Intent(AvatarActivity.this, HomeActivity.class);
                    startActivity(intent);
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
