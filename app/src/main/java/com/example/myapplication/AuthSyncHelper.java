package com.example.myapplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.achievement.GameAchievementDao;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.fragment.achievement.GameAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthSyncHelper {
    private final Context context;
    private final UserDao userDao;
    private final FourPicsOneWordDao fourPicsOneWordDao;
    private final GameAchievementDao gameAchievementDao;
    private final StoryAchievementDao storyAchievementDao;
    private final Executor executor;
    private final Handler mainHandler;
    private final FirebaseFirestore firestore;

    public AuthSyncHelper(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.fourPicsOneWordDao = db.fourPicsOneWordDao();
        this.gameAchievementDao = db.gameAchievementDao();
        this.storyAchievementDao = db.storyAchievementDao();
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
            Log.d("AuthSyncHelper", "FirebaseUser is null");
            runOnMainThread(() -> callback.onError("No authenticated user"));
            return;
        }
        Log.d("AuthSyncHelper", "Starting sync for user: " + firebaseUser.getEmail());


        firestore.collection("user")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("AuthSyncHelper", "Firestore document exists for user: " + firebaseUser.getEmail());

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

                            Log.d("AuthSyncHelper", "Extracted data from Firestore:");
                            Log.d("AuthSyncHelper", "childName: " + childName);
                            Log.d("AuthSyncHelper", "childBirthday: " + childBirthday);
                            Log.d("AuthSyncHelper", "avatarName: " + avatarName);
                            Log.d("AuthSyncHelper", "controlId: " + controlId);
                            Log.d("AuthSyncHelper", "borderName: " + borderName);
                            Log.d("AuthSyncHelper", "avatarResourceId: " + avatarResourceId);
                            Log.d("AuthSyncHelper", "borderResourceId: " + borderResourceId);

                            if (childName == null || childBirthday == null ||
                                    avatarName == null || controlId == null) {
                                Log.d("AuthSyncHelper", "Incomplete profile detected");
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
                            Log.d("AuthSyncHelper", "Created User object with data:");
                            Log.d("AuthSyncHelper", "UID: " + user.getUid());
                            Log.d("AuthSyncHelper", "Email: " + user.getEmail());
                            Log.d("AuthSyncHelper", "Child Name: " + user.getChildName());

                            // Insert into SQLite on background thread
                            executor.execute(() -> {
                                try {
                                    // Delete existing user data
                                    userDao.deleteByUid(firebaseUser.getUid());

                                    // Insert new user data
                                    long userId = userDao.insert(user);
                                    Log.d("AuthSyncHelper", "Inserted new user with ID: " + userId);

                                    // Sync FourPicsOneWord data
                                    syncFourPicsOneWordData(userId, firebaseUser.getEmail());

                                    runOnMainThread(() -> callback.onSuccess("user"));
                                } catch (Exception e) {
                                    Log.e("AuthSyncHelper", "Error inserting user data", e);
                                    Log.e("AuthSyncHelper", "Stack trace: ", e);
                                    runOnMainThread(() -> callback.onError("Failed to save user data: " + e.getMessage()));
                                }
                            });
                        } else {
                            Log.d("AuthSyncHelper", "No user document found in Firestore, checking admin");
                            checkAdminCollection(firebaseUser, callback);
                        }
                    } else {
                        Log.e("AuthSyncHelper", "Firestore task failed", task.getException());
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

    public void syncGameAchievements(String email, long userId) {
        // First get all documents in the gamesdata collection
        firestore.collection("gameachievements")
                .document(email)
                .collection("gamedata")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            try {
                                // Clear existing achievements first
                                gameAchievementDao.deleteAllGameAchievements();

                                // Process each achievement document
                                int achievementsInserted = 0;
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    Map<String, Object> documentData = document.getData();
                                    if (documentData != null) {

                                        if (isValidGameAchievementData(documentData)) {
                                            String gameId = getStringValue(documentData, "gameId");
                                            String title = getStringValue(documentData, "title");
                                            String level = getStringValue(documentData, "level");
                                            String points = getStringValue(documentData, "points");
                                            String isCompleted = getStringValue(documentData, "isCompleted");


                                            GameAchievementModel achievement = new GameAchievementModel(
                                                    gameId,title,level,points,isCompleted
                                            );
                                            long insertedId = gameAchievementDao.insert(achievement);
                                            if (insertedId != -1) {
                                                achievementsInserted++;
                                                Log.d("AuthSyncHelper", "Inserted achievement with ID: " + insertedId +
                                                        ", gameId ID: " + gameId +
                                                        ", Title: " + title);
                                            } else {
                                                Log.w("AuthSyncHelper", "Failed to insert achievement: " + title);
                                            }
                                        }
                                    }
                                }
                                Log.d("AuthSyncHelper", "Successfully synced game achievements for: " + email);
                            } catch (Exception e) {
                                Log.e("AuthSyncHelper", "Error syncing game achievements", e);
                                createDefaultGameAchievements(email);
                            }
                        });
                    } else {
                        Log.e("AuthSyncHelper", "Error fetching game achievements: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        executor.execute(() -> createDefaultGameAchievements(email));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AuthSyncHelper", "Error fetching game achievements from Firestore", e);
                    executor.execute(() -> createDefaultGameAchievements(email));
                });
    }

    private boolean isValidGameAchievementData(Map<String, Object> data) {
        boolean isValid = data != null &&
                data.containsKey("gameId") &&
                data.containsKey("title") &&
                data.containsKey("level") &&
                data.containsKey("points") &&
                data.containsKey("isCompleted");

        if (!isValid) {
            Log.w("AuthSyncHelper", "Invalid achievement data validation:");
            for (String key : new String[]{"gameId", "title", "level", "points", "isCompleted"}) {
                Log.w("AuthSyncHelper", "Key: " + key + ", Present: " + data.containsKey(key));
            }
        }

        return isValid;
    }
    private void createDefaultGameAchievements(String email) {
        try {
            // Create default achievements if needed
            List<GameAchievementModel> defaultAchievements = new ArrayList<>();
            defaultAchievements.add(new GameAchievementModel("game_1", "First Win", "1", "100", "false"));
            defaultAchievements.add(new GameAchievementModel("game_2", "Perfect Score", "1", "200", "false"));
            // Add more default achievements as needed

            for (GameAchievementModel achievement : defaultAchievements) {
                gameAchievementDao.insert(achievement);
            }
            Log.d("AuthSyncHelper", "Created default game achievements for: " + email);
        } catch (Exception e) {
            Log.e("AuthSyncHelper", "Error creating default game achievements", e);
        }
    }

    public void syncStoryAchievements(String email, long userId) {
        Log.d("AuthSyncHelper", "Starting story achievements sync for: " + email);
        firestore.collection("storyachievements")
                .document(email)
                .collection("achievements")
                .get()
                .addOnCompleteListener(task -> {
                    Log.d("AuthSyncHelper", "Firestore query completed. Successful: " + task.isSuccessful());

                    if (task.isSuccessful()) {
                        Log.d("AuthSyncHelper", "Number of documents retrieved: " + task.getResult().size());

                        executor.execute(() -> {
                            try {
                                // Delete existing achievements first
                                Log.d("AuthSyncHelper", "Deleting existing achievements");
                                storyAchievementDao.deleteAllUserAchievements();

                                int achievementsInserted = 0;
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    Map<String, Object> documentData = document.getData();
                                    Log.d("AuthSyncHelper", "Processing document: " + document.getId());

                                    if (documentData != null) {
                                        // Validate and insert achievement
                                        if (isValidAchievementData(documentData)) {
                                            String storyId = getStringValue(documentData, "storyId");
                                            String title = getStringValue(documentData, "title");
                                            String type = getStringValue(documentData, "type");
                                            String isCompleted = getStringValue(documentData, "isCompleted");
                                            int count = getIntValue(documentData, "count");

                                            Log.d("AuthSyncHelper", "Extracted achievement details:");
                                            Log.d("AuthSyncHelper", "StoryId: " + storyId);
                                            Log.d("AuthSyncHelper", "Title: " + title);
                                            Log.d("AuthSyncHelper", "Type: " + type);
                                            Log.d("AuthSyncHelper", "IsCompleted: " + isCompleted);
                                            Log.d("AuthSyncHelper", "Count: " + count);

                                            StoryAchievementModel achievement = new StoryAchievementModel(
                                                    title,
                                                    type,
                                                    isCompleted,
                                                    count,
                                                    storyId
                                            );

                                            long insertedId = storyAchievementDao.insert(achievement);
                                            if (insertedId != -1) {
                                                achievementsInserted++;
                                                Log.d("AuthSyncHelper", "Inserted achievement with ID: " + insertedId +
                                                        ", Story ID: " + storyId +
                                                        ", Title: " + title);
                                            } else {
                                                Log.w("AuthSyncHelper", "Failed to insert achievement: " + title);
                                            }
                                        } else {
                                            Log.w("AuthSyncHelper", "Invalid achievement data: " + documentData);
                                        }
                                    } else {
                                        Log.w("AuthSyncHelper", "No achievements found in document");
                                    }
                                }

                                // Check total achievements count after insertion
                                int totalAchievementsCount = storyAchievementDao.getAchievementsCount();
                                Log.d("AuthSyncHelper", "Total achievements inserted: " + achievementsInserted);
                                Log.d("AuthSyncHelper", "Total achievements in database: " + totalAchievementsCount);

                                Log.d("AuthSyncHelper", "Story achievements sync completed successfully");
                            } catch (Exception e) {
                                Log.e("AuthSyncHelper", "Error during story achievements sync", e);
                                createDefaultStoryAchievements(email);
                            }
                        });
                    } else {
                        Log.e("AuthSyncHelper", "Firestore query failed", task.getException());
                        executor.execute(() -> createDefaultStoryAchievements(email));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AuthSyncHelper", "Firestore query failure", e);
                    executor.execute(() -> createDefaultStoryAchievements(email));
                });
    }

    // Modify validation method to be more verbose
    private boolean isValidAchievementData(Map<String, Object> data) {
        boolean isValid = data != null &&
                data.containsKey("storyId") &&
                data.containsKey("title") &&
                data.containsKey("type") &&
                data.containsKey("isCompleted") &&
                data.containsKey("count");

        if (!isValid) {
            Log.w("AuthSyncHelper", "Invalid achievement data validation:");
            for (String key : new String[]{"storyId", "title", "type", "isCompleted", "count"}) {
                Log.w("AuthSyncHelper", "Key: " + key + ", Present: " + data.containsKey(key));
            }
        }

        return isValid;
    }

    // Helper method to safely get string value
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    // Helper method to safely get integer value
    private int getIntValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    private void createDefaultStoryAchievements(String email) {
        try {
            // Create default achievements if needed
            List<StoryAchievementModel> defaultAchievements = new ArrayList<>();
            defaultAchievements.add(new StoryAchievementModel("First Story", "read", "false", 0, "story_1"));
            defaultAchievements.add(new StoryAchievementModel("Story Master", "complete", "false", 0, "story_2"));
            // Add more default achievements as needed

            for (StoryAchievementModel achievement : defaultAchievements) {
                storyAchievementDao.insert(achievement);
            }
            Log.d("AuthSyncHelper", "Created default story achievements for: " + email);
        } catch (Exception e) {
            Log.e("AuthSyncHelper", "Error creating default story achievements", e);
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