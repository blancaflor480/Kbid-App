package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AvatarPrefs";
    private static final String AVATAR_KEY = "selectedAvatar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar_user);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupAvatarClickListeners();
    }

    private void setupAvatarClickListeners() {
        // Assuming you have these avatars set up in your layout
        CircleImageView bunny = findViewById(R.id.bunny);
        CircleImageView boar = findViewById(R.id.boar);
        CircleImageView crocs = findViewById(R.id.crocs);
        CircleImageView horse = findViewById(R.id.horse);
        CircleImageView noah = findViewById(R.id.noah);
        CircleImageView lion = findViewById(R.id.lion);
        CircleImageView moose = findViewById(R.id.moose);
        CircleImageView koala = findViewById(R.id.koala);
        CircleImageView penguin = findViewById(R.id.penguin);
        CircleImageView tiger = findViewById(R.id.tiger);

        // Set click listeners for each avatar
        bunny.setOnClickListener(view -> selectAvatar(R.drawable.bunny));
        boar.setOnClickListener(view -> selectAvatar(R.drawable.boar));
        crocs.setOnClickListener(view -> selectAvatar(R.drawable.crocs));
        horse.setOnClickListener(view -> selectAvatar(R.drawable.horse));
        noah.setOnClickListener(view -> selectAvatar(R.drawable.noah));
        lion.setOnClickListener(view -> selectAvatar(R.drawable.lion));
        moose.setOnClickListener(view -> selectAvatar(R.drawable.moose));
        koala.setOnClickListener(view -> selectAvatar(R.drawable.koala));
        penguin.setOnClickListener(view -> selectAvatar(R.drawable.penguin));
        tiger.setOnClickListener(view -> selectAvatar(R.drawable.tiger));
    }

    private void selectAvatar(int avatarId) {
        // Save the selected avatar ID to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AVATAR_KEY, avatarId);
        editor.apply();

        // Redirect to HomeActivity
        Intent intent = new Intent(AvatarActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity to prevent the user from going back
    }
}

