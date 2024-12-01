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

                            if (childName == null || childBirthday == null ||
                                    avatarName == null || controlId == null) {
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

                            // Insert into SQLite on background thread
                            executor.execute(() -> {
                                try {
                                    // Delete existing user data
                                    userDao.deleteByUid(firebaseUser.getUid());

                                    // Insert new user data
                                    long userId = userDao.insert(user);

                                    // Ensure FourPicsOneWord entry exists
                                    ensureFourPicsOneWordEntry(userId, firebaseUser.getEmail());

                                    runOnMainThread(() -> callback.onSuccess("user"));
                                } catch (Exception e) {
                                    Log.e("AuthSyncHelper", "Error inserting user data", e);
                                    runOnMainThread(() -> callback.onError("Failed to save user data: " + e.getMessage()));
                                }
                            });
                        } else {
                            // Check admin collection if user document doesn't exist
                            checkAdminCollection(firebaseUser, callback);
                        }
                    } else {
                        // If Firestore fetch fails, still try to handle user and game data
                        handleFirestoreFailure(firebaseUser, callback);
                    }
                });
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

                // Ensure FourPicsOneWord entry exists
                ensureFourPicsOneWordEntry(userId, firebaseUser.getEmail());

                runOnMainThread(() -> callback.onSuccess("user_fallback"));
            } catch (Exception e) {
                Log.e("AuthSyncHelper", "Error in fallback user handling", e);
                runOnMainThread(() -> callback.onError("Fallback user handling failed: " + e.getMessage()));
            }
        });
    }

    private void ensureFourPicsOneWordEntry(long userId, String email) {
        FourPicsOneWord existingEntry = fourPicsOneWordDao.getGameDataWithEmailSync(email);

        if (existingEntry == null) {
            // Create new FourPicsOneWord entry if none exists
            FourPicsOneWord fourPicsOneWord = new FourPicsOneWord(
                    (int) userId,
                    1,  // Starting level
                    0,  // Initial score
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()),
                    email
            );

            fourPicsOneWordDao.insert(fourPicsOneWord);
            Log.d("AuthSyncHelper", "Created new FourPicsOneWord entry for: " + email);
        }
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