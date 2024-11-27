package com.example.myapplication.fragment.useraccount;

import static android.app.PendingIntent.getActivity;
import static android.system.Os.shutdown;
import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.achievement.GameAchievementDao;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class account extends AppCompatActivity {
    private AppDatabase db;
    private UserDao userDao;
    private StoryAchievementDao storyAchievementDao;
    private GameAchievementDao gameAchievementDao;
    private FourPicsOneWordDao fourPicsOneWordDao;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private Executor executor;
    private EditText editemail;
    private FirestoreSyncManager firestoreSyncManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_account);

        // Initialize database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        storyAchievementDao = db.storyAchievementDao();
        gameAchievementDao = db.gameAchievementDao();
        fourPicsOneWordDao = db.fourPicsOneWordDao();
        executor = Executors.newSingleThreadExecutor();
        firestoreSyncManager = new FirestoreSyncManager(this);
        // Firebase Authentication listener
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in, proceed with sync
                firestoreSyncManager.syncUserDataWithFirestore();
                firestoreSyncManager.syncFourPicsOneWordWithFirestore();
                firestoreSyncManager.syncGameAchievementsWithFirestore();
            } else {
                Log.w("FirebaseAuth", "User is signed out");
            }
        });

        // Initialize views
        ImageButton closeButton = findViewById(R.id.close);
        closeButton.setOnClickListener(v -> onBackPressed());
        ImageView avatarImageView = findViewById(R.id.avatar);
        TextView editName = findViewById(R.id.Editname);
        TextView controlNumber = findViewById(R.id.controlnumber);
        CardView editprof = findViewById(R.id.editprof);
        editprof.setOnClickListener(v -> showEditProfileDialog());

        // Load user details
        setupBadgeRecyclerView();
        setupGameBadgeRecyclerView();
        loadUserDetails(editName, controlNumber, avatarImageView);
    }


    private void loadUserDetails(TextView editName, TextView controlNumber, ImageView avatarImageView) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = userDao.getFirstUser();
            runOnUiThread(() -> {
                if (user != null) {
                    // Set name or fallback
                    editName.setText(user.getChildName() != null ? user.getChildName() : "No Name Provided");

                    // Set control ID or fallback, and hide the control number if it's empty
                    if (user.getControlid() == null || user.getControlid().isEmpty()) {
                        controlNumber.setVisibility(View.GONE);  // Hide if no control ID
                    } else {
                        controlNumber.setText("ID: " + user.getControlid());
                        controlNumber.setVisibility(View.VISIBLE);  // Show and set value if present
                    }

                    // Load avatar or placeholder
                    if (user.getAvatarImage() != null) {
                        Glide.with(this)
                                .load(user.getAvatarImage())
                                .placeholder(R.drawable.lion) // Default avatar resource
                                .into(avatarImageView);
                    } else {
                        avatarImageView.setImageResource(R.drawable.lion); // Fallback
                    }
                } else {
                    // No user found
                    Toast.makeText(this, "User not found. Please add a user first.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showEditProfileDialog() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_profile, null);
        builder.setView(dialogView);

        ImageView avatarImageView = dialogView.findViewById(R.id.avatar);
        EditText editName = dialogView.findViewById(R.id.Editname);
        EditText editAge = dialogView.findViewById(R.id.Editage);
        editemail = dialogView.findViewById(R.id.google); // Email field
        TextView controlNumber = dialogView.findViewById(R.id.controlnumber);
        AppCompatButton connectButton = dialogView.findViewById(R.id.connect);
        AppCompatButton saveButton = dialogView.findViewById(R.id.save);
        ImageButton closeButton = dialogView.findViewById(R.id.close);
        ImageButton changeProfileButton = dialogView.findViewById(R.id.changepf);

        executor.execute(() -> {
            User user = userDao.getFirstUser();
            runOnUiThread(() -> {
                if (user != null) {
                    editName.setText(user.getChildName());
                    editAge.setText(String.valueOf(user.getChildBirthday()));
                    editemail.setText(user.getEmail());
                    controlNumber.setText(user.getControlid());

                    if (user.getEmail() == null || user.getEmail().isEmpty() || "Null".equalsIgnoreCase(user.getEmail())) {
                        connectButton.setVisibility(View.VISIBLE);
                    } else {
                        connectButton.setVisibility(View.GONE);
                    }

                    Glide.with(this)
                            .load(user.getAvatarResourceId())
                            .into(avatarImageView);
                }
            });
        });

        changeProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvatarSelectionActivity.class);
            startActivityForResult(intent, 1);
        });

        connectButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        AlertDialog dialog = builder.create();
        closeButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String newName = editName.getText().toString();
            executor.execute(() -> {
                User user = userDao.getFirstUser();
                if (user != null) {
                    user.setChildName(newName);
                    userDao.updateUser(user);
                    runOnUiThread(() -> {
                        editName.setText(newName);
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                editemail.setText(email);

                // Get Firebase user UID
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = firebaseUser != null ? firebaseUser.getUid() : null;

                // Save data to SQLite
                saveUserDataToSQLite(uid, email);

                // Check if email is already in Firebase Authentication
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<String> signInMethods = task.getResult().getSignInMethods();
                                if (signInMethods == null || signInMethods.isEmpty()) {
                                    // Email is not registered, bind email
                                    bindEmailToApp(email, uid);
                                } else {
                                    // Email exists, no need to bind
                                    Toast.makeText(this, "Email already bound!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Error checking sign-in methods: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void saveUserDataToSQLite(String uid, String email) {
        // Execute this on a background thread
        executor.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null) {
                user.setUid(uid);   // Assuming your User model has a field for UID
                user.setEmail(email);  // Assuming your User model has a field for email

                // Update the user in SQLite
                userDao.updateUser(user);

                // Notify the user in the UI thread
                runOnUiThread(() -> {
                    Toast.makeText(this, "User data saved to SQLite.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }



    private void bindEmailToApp(String email, String uid) {
        // Save email to SQLite and Firestore
        executor.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null) {
                // Update user with Firebase UID
                user.setEmail(email);
                user.setUid(uid);  // Ensure your User model has a Firebase UID field
                userDao.updateUser(user);

                // Notify the user in UI thread
                runOnUiThread(() -> {
                    editemail.setText(email);
                    Toast.makeText(this, "Email bound successfully in SQLite.", Toast.LENGTH_SHORT).show();
                });

                // Log to confirm the data to be saved
                Log.d("FirebaseBinding", "User to be saved to Firestore: " + user.toString());  // Check the object details

                // Save the user data to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("user")
                        .document(email)  // Use email as document ID or use Firebase UID
                        .set(user.toMap())  // Ensure User class has a method `toMap()` to convert the object to a map
                        .addOnSuccessListener(aVoid -> {
                            // Log successful insertion
                            Log.d("FirebaseBinding", "Data successfully saved to Firestore.");
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Email bound successfully in Firestore.", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Log failure reason
                            Log.e("FirebaseBinding", "Failed to update Firestore: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            } else {
                // Log if no user found
                Log.e("FirebaseBinding", "No user found to bind email.");
            }
        });
    }

    private void setupBadgeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclep);

        // Set up a GridLayoutManager with 3 items per row
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        // Prepare the badge list
        List<Badge> badges = new ArrayList<>();

        // Fetch achievements asynchronously
        executor.execute(() -> {
            try {
                // Fetch achievements from the database
                int completedStories = storyAchievementDao.getCompletedStoryCount();

                // Map achievements to badges based on completed stories
                badges.add(new Badge(
                        getBadgeImageResource(1, completedStories >= 1),
                        completedStories >= 1,
                        true,
                        false
                ));
                badges.add(new Badge(
                        getBadgeImageResource(10, completedStories >= 10),
                        completedStories >= 10,
                        true,
                        completedStories >= 10
                ));
                badges.add(new Badge(
                        getBadgeImageResource(20, completedStories >= 20),
                        completedStories >= 20,
                        true,
                        completedStories >= 20
                ));
                badges.add(new Badge(
                        getBadgeImageResource(30, completedStories >= 30),
                        completedStories >= 30,
                        true,
                        completedStories >= 30
                ));
                badges.add(new Badge(
                        getBadgeImageResource(50, completedStories >= 50),
                        completedStories >= 50,
                        true,
                        completedStories >= 50
                ));

                // Update UI on the main thread
                runOnUiThread(() -> {
                    BadgeAdapter adapter = new BadgeAdapter(this, badges);
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load badges", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private int getBadgeImageResource(int milestone, boolean isUnlocked) {
        if (isUnlocked) {
            switch (milestone) {
                case 1:
                    return R.drawable.unlockstory1; // Replace with the actual drawable
                case 10:
                    return R.drawable.unlockstory10; // Replace with the actual drawable
                case 20:
                    return R.drawable.unlockstory20; // Replace with the actual drawable
                case 30:
                    return R.drawable.unlockstory30; // Replace with the actual drawable
                case 50:
                    return R.drawable.unlockstory50; // Replace with the actual drawable
            }
        }
        return R.raw.lock; // Default locked badge image
    }

    private void setupGameBadgeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclepgame);

        // Set up a GridLayoutManager with 3 items per row
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        // Prepare the badge list
        List<Badge> badges = new ArrayList<>();

        // Fetch achievements asynchronously
        executor.execute(() -> {
            try {
                // Fetch achievements from the database
                int completedGames = gameAchievementDao.getCompletedGamesCount();

                // Map achievements to badges based on completed stories
                badges.add(new Badge(
                        getGameBadgeImageResource(1, completedGames >= 1),
                        completedGames >= 1,
                        true,
                        false
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(15, completedGames>= 15),
                        completedGames >= 15,
                        true,
                        completedGames >= 15
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(30, completedGames >= 30),
                        completedGames >= 30,
                        true,
                        completedGames >= 30
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(50, completedGames >= 50),
                        completedGames >= 50,
                        true,
                        completedGames >= 50
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(70, completedGames >= 70),
                        completedGames >= 70,
                        true,
                        completedGames >= 70
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(90, completedGames >= 90),
                        completedGames >= 90,
                        true,
                        completedGames >= 90
                ));
                badges.add(new Badge(
                        getGameBadgeImageResource(100, completedGames >= 100),
                        completedGames >= 100,
                        true,
                        completedGames >= 100
                ));

                // Update UI on the main thread
                runOnUiThread(() -> {
                    GameBadgeAdapter adapter = new GameBadgeAdapter(this, badges);
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load badges", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private int getGameBadgeImageResource(int milestone, boolean isUnlocked) {
        if (isUnlocked) {
            switch (milestone) {
                case 1:
                    return R.drawable.bronze1; // Replace with the actual drawable
                case 15:
                    return R.drawable.bronze2; // Replace with the actual drawable
                case 30:
                    return R.drawable.bronze3; // Replace with the actual drawable
                case 50:
                    return R.drawable.silver1; // Replace with the actual drawable
                case 70:
                    return R.drawable.silver2; // Replace with the actual drawable
                case 90:
                    return R.drawable.silver3;
                case 100:
                    return R.drawable.gold; // Replace with the actual drawable
            }
        }
        return R.raw.lock; // Default locked badge image
    }





}
