package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarActivity extends AppCompatActivity {

    private AppDatabase db;
    private UserDao userDao;
    private FourPicsOneWordDao fourPicsOneWordDao;
    private User currentUser;
    private Handler handler;
    private Runnable loadingRunnable;
    private TextView loadingTextView;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar_user);

        // Initialize the database and DAOs
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        fourPicsOneWordDao = db.fourPicsOneWordDao();

        // Get data from intent
        Intent intent = getIntent();
        String controlId = intent.getStringExtra("CONTROL_ID");
        userEmail = intent.getStringExtra("USER_EMAIL");
        String firstname = intent.getStringExtra("FIRSTNAME");
        String birthday = intent.getStringExtra("BIRTHDAY");

        // Save user data locally
        saveUserData(controlId, userEmail, firstname, birthday);
    }

    private void saveUserData(String controlId, String userEmail, String firstname, String birthday) {
        AsyncTask.execute(() -> {
            // Create new user object
            User user = new User();
            user.setControlid(controlId);
            user.setEmail(userEmail);
            user.setChildName(firstname);
            user.setChildBirthday(birthday);

            // Insert or update user in local database
            long userId = userDao.insert(user);

            // Load the current user and proceed with avatar setup
            currentUser = userDao.getFirstUser();
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
            final String avatarName = getResources().getResourceEntryName(avatarResourceIds[i]);
            final int resourceId = avatarResourceIds[i];
            avatars[i].setOnClickListener(view -> selectAvatar(avatarName, resourceId));
        }
    }

    private void showLoadingAnimation() {
        loadingTextView = findViewById(R.id.text);
        loadingRunnable = new Runnable() {
            private int dotCount = 0;

            @Override
            public void run() {
                String text = "LOADING" + new String(new char[dotCount]).replace("\0", ".");
                loadingTextView.setText(text);
                dotCount = (dotCount + 1) % 4;
                handler.postDelayed(this, 500);
            }
        };

        handler = new Handler();
        handler.post(loadingRunnable);
    }

    private void selectAvatar(String avatarName, int avatarResourceId) {
        Drawable drawable = getResources().getDrawable(avatarResourceId);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        byte[] avatarImage = bitmapToByteArray(bitmap);

        AsyncTask.execute(() -> {
            if (currentUser != null) {
                currentUser.setAvatarName(avatarName);
                currentUser.setAvatarResourceId(avatarResourceId);
                currentUser.setAvatarImage(avatarImage);

                userDao.updateUser(currentUser);

                // Create FourPicsOneWord entry if not exists
                FourPicsOneWord existingEntry = fourPicsOneWordDao.getGameDataWithEmailSync(userEmail);
                if (existingEntry == null) {
                    FourPicsOneWord fourPicsOneWord = new FourPicsOneWord(
                            (int) currentUser.getId(),
                            1,  // Starting level
                            0,  // Initial score
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()),
                            userEmail
                    );
                    fourPicsOneWordDao.insert(fourPicsOneWord);
                }

                runOnUiThread(() -> {
                    setContentView(R.layout.loader_homepage);
                    loadingTextView = findViewById(R.id.text);
                    showLoadingAnimation();

                    new Handler().postDelayed(() -> {
                        if (handler != null) handler.removeCallbacks(loadingRunnable);
                        Intent intent = new Intent(AvatarActivity.this, HomeActivity.class);
                        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
                        startActivity(intent);
                        finish();
                    }, 5000);
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