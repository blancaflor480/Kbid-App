package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthSyncHelper {
    private final Context context;
    private final UserDao userDao;
    private final FourPicsOneWordDao fourPicsOneWordDao;
    private final Executor executor;
    private final Handler mainHandler;
    private final FirebaseFirestore firestore;

    public AuthSyncHelper(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.fourPicsOneWordDao = db.fourPicsOneWordDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface SyncCallback {
        void onSuccess(String userType);
        void onError(String message);
    }

    private void runOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public void syncUserDataFromFirestore(FirebaseUser firebaseUser, SyncCallback callback) {
        if (firebaseUser == null) {
            runOnMainThread(() -> callback.onError("No authenticated user"));
            return;
        }

        firestore.collection("user")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Extract user data
                            String childName = document.getString("childName");
                            String childBirthday = document.getString("childBirthday");
                            String avatarName = document.getString("avatarName");
                            String controlId = document.getString("controlId");
                            Integer avatarResourceId = document.getLong("avatarResourceId") != null ?
                                    document.getLong("avatarResourceId").intValue() : null;

                            String borderName = document.getString("borderName");
                            Integer borderResourceId = document.getLong("borderResourceId") != null ?
                                    document.getLong("borderResourceId").intValue() : null;

                            if (childName == null || childBirthday == null ||
                                    avatarName == null || borderName == null || controlId == null) {
                                runOnMainThread(() -> callback.onSuccess("incomplete_profile"));
                                return;
                            }

                            // Create User object
                            User user = new User();
                            user.setUid(firebaseUser.getUid());
                            user.setEmail(firebaseUser.getEmail());
                            user.setChildName(childName);
                            user.setChildBirthday(childBirthday);
                            user.setAvatarName(avatarName);
                            user.setControlid(controlId);
                            if (avatarResourceId != null) {
                                user.setAvatarResourceId(avatarResourceId);
                            }
                            user.setBorderName(borderName);
                            if (borderResourceId != null) {
                                user.setAvatarResourceId(borderResourceId);
                            }

                            // Insert into SQLite on background thread
                            executor.execute(() -> {
                                try {
                                    // Delete existing user data
                                    userDao.deleteByUid(firebaseUser.getUid());

                                    // Insert new user data
                                    long userId = userDao.insert(user);

                                    // Sync FourPicsOneWord data
                                    syncFourPicsOneWordData(userId, firebaseUser.getEmail());

                                    runOnMainThread(() -> callback.onSuccess("user"));
                                } catch (Exception e) {
                                    Log.e("AuthSyncHelper", "Error inserting user data", e);
                                    runOnMainThread(() -> callback.onError("Failed to save user data: " + e.getMessage()));
                                }
                            });
                        } else {
                            checkAdminCollection(firebaseUser, callback);
                        }
                    } else {
                        handleFirestoreFailure(firebaseUser, callback);
                    }
                });
    }

    private void syncFourPicsOneWordData(long userId, String email) {
        // First check Firestore for existing FourPicsOneWord data
        firestore.collection("fourpicsoneword")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Found existing data in Firestore
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // Get the data from Firestore matching the exact field names
                        Integer currentLevel = document.getLong("currentLevel") != null ?
                                document.getLong("currentLevel").intValue() : 1;
                        Integer points = document.getLong("points") != null ?
                                document.getLong("points").intValue() : 0;
                        String date = document.getString("date");

                        if (date == null) {
                            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(new Date());
                        }

                        // Create FourPicsOneWord object with Firestore data
                        FourPicsOneWord gameData = new FourPicsOneWord(
                                (int) userId,
                                currentLevel,
                                points,
                                date,
                                email
                        );

                        // Update SQLite on background thread
                        executor.execute(() -> {
                            try {
                                FourPicsOneWord existingEntry =
                                        fourPicsOneWordDao.getGameDataWithEmailSync(email);

                                if (existingEntry != null) {
                                    gameData.setId(existingEntry.getId()); // Preserve local ID
                                    fourPicsOneWordDao.updategames(gameData);
                                    Log.d("AuthSyncHelper", "Updated existing FourPicsOneWord from Firestore for: " + email);
                                } else {
                                    fourPicsOneWordDao.insert(gameData);
                                    Log.d("AuthSyncHelper", "Inserted new FourPicsOneWord from Firestore for: " + email);
                                }
                            } catch (Exception e) {
                                Log.e("AuthSyncHelper", "Error syncing FourPicsOneWord data from Firestore", e);
                                throw e;
                            }
                        });
                    } else {
                        // No data in Firestore, use default values
                        executor.execute(() -> {
                            try {
                                FourPicsOneWord existingEntry =
                                        fourPicsOneWordDao.getGameDataWithEmailSync(email);

                                if (existingEntry == null) {
                                    // Create new entry with default values matching the entity class
                                    FourPicsOneWord newEntry = new FourPicsOneWord(
                                            (int) userId,
                                            1,  // Default currentLevel
                                            0,  // Default points
                                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                    .format(new Date()),
                                            email
                                    );
                                    fourPicsOneWordDao.insert(newEntry);
                                    Log.d("AuthSyncHelper", "Created new default FourPicsOneWord entry for: " + email);
                                } else {
                                    // Update existing entry's userId
                                    existingEntry.setUserId((int) userId);
                                    fourPicsOneWordDao.updategames(existingEntry);
                                    Log.d("AuthSyncHelper", "Updated existing FourPicsOneWord userId for: " + email);
                                }
                            } catch (Exception e) {
                                Log.e("AuthSyncHelper", "Error handling default FourPicsOneWord data", e);
                                throw e;
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AuthSyncHelper", "Error checking Firestore for FourPicsOneWord data", e);
                    // Handle Firestore failure by falling back to default values
                    executor.execute(() -> createDefaultFourPicsOneWordEntry(userId, email));
                });
    }

    private void createDefaultFourPicsOneWordEntry(long userId, String email) {
        try {
            FourPicsOneWord existingEntry = fourPicsOneWordDao.getGameDataWithEmailSync(email);
            if (existingEntry == null) {
                FourPicsOneWord newEntry = new FourPicsOneWord(
                        (int) userId,
                        1,  // Default currentLevel
                        0,  // Default points
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()),
                        email
                );
                fourPicsOneWordDao.insert(newEntry);
                Log.d("AuthSyncHelper", "Created new default FourPicsOneWord entry after Firestore failure for: " + email);
            }
        } catch (Exception e) {
            Log.e("AuthSyncHelper", "Error creating default FourPicsOneWord entry", e);
            throw e;
        }
    }


    private void handleFirestoreFailure(FirebaseUser firebaseUser, SyncCallback callback) {
        executor.execute(() -> {
            try {
                // Check if user already exists in local database
                User existingUser = userDao.getUserByEmail(firebaseUser.getEmail());

                long userId;
                if (existingUser == null) {
                    // Create a minimal user entry if not exists
                    User user = new User();
                    user.setUid(firebaseUser.getUid());
                    user.setEmail(firebaseUser.getEmail());
                    userId = userDao.insert(user);
                } else {
                    userId = existingUser.getId();
                }

                // Sync FourPicsOneWord data
                syncFourPicsOneWordData(userId, firebaseUser.getEmail());

                runOnMainThread(() -> callback.onSuccess("user_fallback"));
            } catch (Exception e) {
                Log.e("AuthSyncHelper", "Error in fallback user handling", e);
                runOnMainThread(() -> callback.onError("Fallback user handling failed: " + e.getMessage()));
            }
        });
    }

    private void checkAdminCollection(FirebaseUser firebaseUser, SyncCallback callback) {
        firestore.collection("admin")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if (role != null && (role.equals("Teacher") || role.equals("SuperAdmin"))) {
                                runOnMainThread(() -> callback.onSuccess("admin"));
                            } else {
                                runOnMainThread(() -> callback.onSuccess("invalid"));
                            }
                        } else {
                            runOnMainThread(() -> callback.onSuccess("invalid"));
                        }
                    } else {
                        runOnMainThread(() -> callback.onError("Failed to check admin status: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error")));
                    }
                });
    }
}