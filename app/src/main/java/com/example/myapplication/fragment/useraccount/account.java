package com.example.myapplication.fragment.useraccount;

import static android.app.PendingIntent.getActivity;
import static android.system.Os.shutdown;
import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.stream.Collectors;

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
    private AppCompatButton changepassword;


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
        storyAchievementDao = db.storyAchievementDao();
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
                firestoreSyncManager.syncStoryAchievementsWithFirestore();

                //firestoreSyncManager.syncDataFromFirestore();
            } else {
                Log.w("FirebaseAuth", "User is signed out");
            }
        });


        // Initialize views
        ImageButton closeButton = findViewById(R.id.close);
        closeButton.setOnClickListener(v -> onBackPressed());
        ImageView avatarImageView = findViewById(R.id.avatar);
        ImageView borderImageView = findViewById(R.id.border);
        TextView editName = findViewById(R.id.Editname);
        TextView controlNumber = findViewById(R.id.controlnumber);
        CardView editprof = findViewById(R.id.editprof);
        editprof.setOnClickListener(v -> showEditProfileDialog());

        // Load user details
        setupBadgeDevotionalRecyclerView();
        setupBadgeRecyclerView();
        setupGameBadgeRecyclerView();
        loadUserDetails(editName, controlNumber, avatarImageView, borderImageView);
    }


    private void loadUserDetails(TextView editName, TextView controlNumber, ImageView avatarImageView, ImageView borderImageView) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = userDao.getFirstUser();
            runOnUiThread(() -> {
                if (user != null) {
                    // Set name or fallback
                    editName.setText(user.getChildName() != null ? user.getChildName() : "No Name Provided");

                    // Set control ID or fallback, and hide the control number if it's empty
                    if (user.getControlid() == null || user.getControlid().isEmpty()) {
                        controlNumber.setVisibility(View.GONE);
                    } else {
                        controlNumber.setText("ID: " + user.getControlid());
                        controlNumber.setVisibility(View.VISIBLE);
                    }

                    // Load avatar or placeholder
                    if (user.getAvatarImage() != null) {
                        Glide.with(this)
                                .load(user.getAvatarImage())
                                .placeholder(R.drawable.lion)
                                .into(avatarImageView);
                    } else if (user.getAvatarName() != null && !user.getAvatarName().isEmpty()) {
                        int resourceId = getResources().getIdentifier(
                                user.getAvatarName().toLowerCase(),
                                "drawable",
                                getPackageName()
                        );
                        if (resourceId != 0) {
                            // Resource found, load it
                            avatarImageView.setImageResource(resourceId);
                        } else {
                            // Resource not found, use default
                            avatarImageView.setImageResource(R.drawable.lion);
                        }

                    } else {
                        avatarImageView.setImageResource(R.drawable.lion);

                    }

                    // Enhanced border image loading logic
                    if (user.getBorderImage() != null) {
                        // If borderImage exists in database, load it directly
                        Glide.with(this)
                                .load(user.getBorderImage())
                                .placeholder(R.drawable.bronze)
                                .into(borderImageView);
                    } else if (user.getBorderName() != null && !user.getBorderName().isEmpty()) {
                        // If borderImage is null but borderName exists, load from drawable
                        int resourceId = getResources().getIdentifier(
                                user.getBorderName().toLowerCase(),
                                "drawable",
                                getPackageName()
                        );

                        if (resourceId != 0) {
                            // Resource found, load it
                            borderImageView.setImageResource(resourceId);
                        } else {
                            // Resource not found, use default
                            borderImageView.setImageResource(R.drawable.bronze);
                        }
                    } else {
                        // No border image or name, use default
                        borderImageView.setImageResource(R.drawable.bronze);
                    }
                } else {
                    // No user found
                    Toast.makeText(this, "User not found. Please add a user first.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_changepasswordkids, null);
        builder.setView(dialogView);

        // Initialize views
        EditText currentPassword = dialogView.findViewById(R.id.currentpassword);
        EditText newPassword = dialogView.findViewById(R.id.confirmpassword);
        EditText confirmPassword = dialogView.findViewById(R.id.Editage);
        AppCompatButton saveButton = dialogView.findViewById(R.id.save);
        ImageButton closeButton = dialogView.findViewById(R.id.close);

        AlertDialog dialog = builder.create();

        // Handle close button click
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Handle save button click
        saveButton.setOnClickListener(v -> {
            String currentPass = currentPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            // Validate inputs
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Password Change")
                    .setMessage("Are you sure you want to change your password?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // Get current Firebase user
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && user.getEmail() != null) {
                            // First, reauthenticate the user
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

                            user.reauthenticate(credential)
                                    .addOnCompleteListener(reauthTask -> {
                                        if (reauthTask.isSuccessful()) {
                                            // Reauthentication successful, now change the password
                                            user.updatePassword(newPass)
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(this, "Failed to update password: " +
                                                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Authentication failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });

        dialog.show();
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

        AppCompatButton changepassword = dialogView.findViewById(R.id.changepassbtn);
        if (changepassword != null) {
            changepassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        ImageView avatarImageView = dialogView.findViewById(R.id.avatar);
        ImageView borderImageView = dialogView.findViewById(R.id.border);
        EditText editName = dialogView.findViewById(R.id.Editname);
        EditText editAge = dialogView.findViewById(R.id.Editage);
        TextView titlebirthday = dialogView.findViewById(R.id.titlebirthday);
        editemail = dialogView.findViewById(R.id.google);
        TextView controlNumber = dialogView.findViewById(R.id.controlnumber);
        AppCompatButton connectButton = dialogView.findViewById(R.id.connect);
        AppCompatButton saveButton = dialogView.findViewById(R.id.save);
        AppCompatButton changeBorder = dialogView.findViewById(R.id.changeborder);
        ImageButton closeButton = dialogView.findViewById(R.id.close);
        ImageButton changeProfileButton = dialogView.findViewById(R.id.changepf);


        executor.execute(() -> {
            User user = userDao.getFirstUser();
            runOnUiThread(() -> {
                if (user != null) {
                    // Set basic user information
                    editName.setText(user.getChildName());

                    editAge.setText(String.valueOf(user.getChildBirthday()));

                    editemail.setText(user.getEmail());

                    if (user.getControlid() == null || user.getControlid().isEmpty()) {
                        controlNumber.setVisibility(View.GONE);
                        editAge.setVisibility(View.GONE);
                        titlebirthday.setVisibility(View.GONE);
                    } else {
                        controlNumber.setText("ID: " + user.getControlid());
                        controlNumber.setVisibility(View.VISIBLE);
                        editAge.setVisibility(View.VISIBLE);  // Show editAge when controlId exists
                        titlebirthday.setVisibility(View.VISIBLE);
                        editAge.setText(String.valueOf(user.getChildBirthday()));
                    }

                    // Handle connect button visibility
                    if (user.getEmail() == null || user.getEmail().isEmpty() || "Null".equalsIgnoreCase(user.getEmail())) {
                        connectButton.setVisibility(View.VISIBLE);
                    } else {
                        connectButton.setVisibility(View.GONE);
                    }

                    // Handle avatar image loading
                    if (user.getAvatarImage() != null) {
                        // Load direct image data if available
                        Glide.with(this)
                                .load(user.getAvatarImage())
                                .placeholder(R.drawable.lion)
                                .into(avatarImageView);
                    } else if (user.getAvatarName() != null && !user.getAvatarName().isEmpty()) {
                        // Load from drawable using avatarName
                        int avatarResourceId = getResources().getIdentifier(
                                user.getAvatarName().toLowerCase(),
                                "drawable",
                                getPackageName()
                        );
                        if (avatarResourceId != 0) {
                            Glide.with(this)
                                    .load(avatarResourceId)
                                    .placeholder(R.drawable.lion)
                                    .into(avatarImageView);
                        } else {
                            avatarImageView.setImageResource(R.drawable.lion);
                        }
                    } else {
                        // Default avatar
                        avatarImageView.setImageResource(R.drawable.lion);
                    }

                    // Handle border image loading
                    if (user.getBorderImage() != null) {
                        // Load direct border image data if available
                        Glide.with(this)
                                .load(user.getBorderImage())
                                .placeholder(R.drawable.bronze)
                                .into(borderImageView);
                    } else if (user.getBorderName() != null && !user.getBorderName().isEmpty()) {
                        // Load from drawable using borderName
                        int borderResourceId = getResources().getIdentifier(
                                user.getBorderName().toLowerCase(),
                                "drawable",
                                getPackageName()
                        );
                        if (borderResourceId != 0) {
                            Glide.with(this)
                                    .load(borderResourceId)
                                    .placeholder(R.drawable.bronze)
                                    .into(borderImageView);
                        } else {
                            borderImageView.setImageResource(R.drawable.bronze);
                        }
                    } else {
                        // Default border
                        borderImageView.setImageResource(R.drawable.bronze);
                    }
                }
            });
        });

        changeProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvatarSelectionActivity.class);
            startActivityForResult(intent, 1);
        });

        changeBorder.setOnClickListener(v -> {
            Intent intent = new Intent(this, BorderSelection.class);
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


    private void setupBadgeDevotionalRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclepdevotion);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                runOnUiThread(() -> {
                    db.collection("kidsReflection")
                            .whereEqualTo("email", user.getEmail())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<DevotionalAchievementModel> devotionalBadges = new ArrayList<>();

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                                    // Add Consistent Reflector badge
                                    boolean consistentReflector = Boolean.TRUE.equals(document.getBoolean("Consistent Reflector"));
                                    devotionalBadges.add(new DevotionalAchievementModel(
                                            UUID.randomUUID().toString(),
                                            getDrawableResourceByName("consistentreflector"),
                                            "Consistent Reflector",
                                            user.getEmail(),
                                            user.getControlid()
                                    ));

                                    // Add Creative Contribution badge
                                    boolean creativeContribution = Boolean.TRUE.equals(document.getBoolean("Creative Contribution"));
                                    devotionalBadges.add(new DevotionalAchievementModel(
                                            UUID.randomUUID().toString(),
                                            getDrawableResourceByName("creativecontribution"),
                                            "Creative Contribution",
                                            user.getEmail(),
                                            user.getControlid()
                                    ));

                                    // Add Start Thinker badge
                                    boolean startThinker = Boolean.TRUE.equals(document.getBoolean("Start Thinker"));
                                    devotionalBadges.add(new DevotionalAchievementModel(
                                            UUID.randomUUID().toString(),
                                            getDrawableResourceByName("startthinker"),
                                            "Start Thinker",
                                            user.getEmail(),
                                            user.getControlid()
                                    ));
                                } else {
                                    // Add locked badges if no achievements found
                                    for (int i = 0; i < 3; i++) {
                                        devotionalBadges.add(new DevotionalAchievementModel(R.raw.lock));
                                    }
                                }

                                DevotionalAchievementAdapter adapter = new DevotionalAchievementAdapter(account.this, convertToBadges(devotionalBadges));
                                recyclerView.setAdapter(adapter);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DevotionalBadges", "Failed to load achievements", e);
                                // Add locked badges on failure
                                List<DevotionalAchievementModel> lockedBadges = new ArrayList<>();
                                for (int i = 0; i < 3; i++) {
                                    lockedBadges.add(new DevotionalAchievementModel(R.raw.lock));
                                }
                                DevotionalAchievementAdapter adapter = new DevotionalAchievementAdapter(account.this, convertToBadges(lockedBadges));
                                recyclerView.setAdapter(adapter);
                            });
                });
            } else {
                runOnUiThread(() -> {
                    // Show locked badges when no user or email found
                    List<DevotionalAchievementModel> lockedBadges = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        lockedBadges.add(new DevotionalAchievementModel(R.raw.lock));
                    }
                    DevotionalAchievementAdapter adapter = new DevotionalAchievementAdapter(account.this, convertToBadges(lockedBadges));
                    recyclerView.setAdapter(adapter);
                });
            }
        });
    }

    // Helper method to get drawable resource by name
    private int getDrawableResourceByName(String resourceName) {
        int resourceId = getResources().getIdentifier(
                resourceName.toLowerCase(),
                "drawable",
                getPackageName()
        );
        return resourceId != 0 ? resourceId : R.raw.lock;
    }

    // Helper method to convert DevotionalAchievementModel to Badge
    private List<Badge> convertToBadges(List<DevotionalAchievementModel> devotionalModels) {
        return devotionalModels.stream()
                .map(model -> new Badge(
                        model.getBadge(),
                        model.getBadge() != R.raw.lock,
                        true,
                        model.getBadge() != R.raw.lock
                ))
                .collect(Collectors.toList());
    }

}
