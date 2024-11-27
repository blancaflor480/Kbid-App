package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthSyncHelper {
    private final Context context;
    private final UserDao userDao;
    private final Executor executor;
    private final Handler mainHandler;
    private final FirebaseFirestore firestore;

    public AuthSyncHelper(Context context) {
        this.context = context;
        this.userDao = AppDatabase.getDatabase(context).userDao();
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
                                // If any required field is missing, redirect to ChildNameActivity
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
                                    userDao.deleteByUid(firebaseUser.getUid());
                                    userDao.insert(user);
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
                        runOnMainThread(() -> callback.onError("Failed to fetch user data: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error")));
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