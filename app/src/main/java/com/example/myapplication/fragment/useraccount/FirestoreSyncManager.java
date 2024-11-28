package com.example.myapplication.fragment.useraccount;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.achievement.GameAchievementDao;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.achievement.GameAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirestoreSyncManager {
    private Executor executor;
    private Context context;
    private UserDao userDao;
    private FourPicsOneWordDao fourPicsOneWordDao;
    private GameAchievementDao gameAchievementDao;
    private StoryAchievementDao storyAchievementDao;

    public FirestoreSyncManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getDatabase(context);

        this.userDao = db.userDao();
        this.fourPicsOneWordDao = db.fourPicsOneWordDao();
        this.gameAchievementDao = db.gameAchievementDao();
        this.storyAchievementDao = db.storyAchievementDao();
    }

    public void syncUserDataWithFirestore() {
        // Ensure you're on a background thread
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(() -> {
            try {
                User user = userDao.getFirstUser();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                // Additional null checks
                if (user == null || firebaseUser == null || firebaseUser.getEmail() == null) {
                    Log.w("FirestoreSync", "User or Firebase user is null");
                    return;
                }

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                // Prepare user data map
                Map<String, Object> userData = new HashMap<>();
                userData.put("uid", firebaseUser.getUid());
                userData.put("email", firebaseUser.getEmail());
                userData.put("childName", user.getChildName());
                userData.put("childBirthday", user.getChildBirthday());
                userData.put("avatarName", user.getAvatarName());
                userData.put("controlId", user.getControlid());
                userData.put("avatarResourceId", user.getAvatarResourceId());

                // Optional: Convert avatar image to Base64 if needed

                // Use Firebase UID as document ID for more reliability
                firestore.collection("user")
                        .document(firebaseUser.getUid())
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreSync", "User data successfully synced");
                            runOnMainThread(() -> {
                                Toast.makeText(context, "Profile synced with cloud", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreSync", "Failed to sync user data", e);
                            runOnMainThread(() -> {
                                Toast.makeText(context, "Failed to sync profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            } catch (Exception e) {
                Log.e("FirestoreSync", "Unexpected error during sync", e);
                runOnMainThread(() -> {
                    Toast.makeText(context, "Sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void syncFourPicsOneWordWithFirestore() {
        executor.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null || firebaseUser.getEmail() == null) {
                    Log.w("FirestoreSync", "Firebase user or email is null");
                    return;
                }

                // Get game data using email from Firebase Auth
                FourPicsOneWord gameData = fourPicsOneWordDao.getGameDataWithEmailSync(firebaseUser.getEmail());
                if (gameData == null) {
                    Log.w("FirestoreSync", "No FourPicsOneWord data found for email: " + firebaseUser.getEmail());
                    return;
                }

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                Map<String, Object> data = new HashMap<>();
                data.put("uid", firebaseUser.getUid());
                data.put("email", gameData.getEmail());
                data.put("userId", gameData.getUserId());
                data.put("currentLevel", gameData.getCurrentLevel());
                data.put("points", gameData.getPoints());
                data.put("date", gameData.getDate());

                firestore.collection("fourpicsoneword")
                        .document(firebaseUser.getEmail()) // Use email as document ID
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreSync", "Game data successfully synced");
                            runOnMainThread(() -> Toast.makeText(context, "Game data synced with cloud", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreSync", "Failed to sync game data", e);
                            runOnMainThread(() -> Toast.makeText(context, "Failed to sync game data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });

            } catch (Exception e) {
                Log.e("FirestoreSync", "Unexpected error during game data sync", e);
                runOnMainThread(() -> Toast.makeText(context, "Game sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void syncGameAchievementsWithFirestore() {
        executor.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null || firebaseUser.getEmail() == null) {
                    Log.w("FirestoreSync", "Firebase user or email is null");
                    return;
                }

                // Get FourPicsOneWord data first for the email
                FourPicsOneWord gameData = fourPicsOneWordDao.getGameDataWithEmailSync(firebaseUser.getEmail());
                if (gameData == null) {
                    Log.w("FirestoreSync", "No FourPicsOneWord data found for email: " + firebaseUser.getEmail());
                    return;
                }

                // Get achievements for the user's game data
                List<GameAchievementModel> achievements = gameAchievementDao.getAllAchievements();
                if (achievements.isEmpty()) {
                    Log.w("FirestoreSync", "No achievements found for user");
                    return;
                }

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                WriteBatch batch = firestore.batch();

                // Sync game data first
                DocumentReference gameAchievementsRef = firestore.collection("gameachievements")
                        .document(firebaseUser.getEmail())
                        .collection("gamesdata")
                        .document("gameInfo");

                Map<String, Object> gameDataMap = new HashMap<>();
                gameDataMap.put("userId", gameData.getUserId());
                gameDataMap.put("email", gameData.getEmail());
                gameDataMap.put("points", gameData.getPoints());
                gameDataMap.put("currentLevel", gameData.getCurrentLevel());
                batch.set(gameAchievementsRef, gameDataMap, SetOptions.merge());

                // Sync achievements
                for (GameAchievementModel achievement : achievements) {
                    DocumentReference achievementRef = firestore.collection("gameachievements")
                            .document(firebaseUser.getEmail())
                            .collection("gamesdata")
                            .document("achievements_" + achievement.getId());

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", firebaseUser.getUid());
                    data.put("email", firebaseUser.getEmail());
                    data.put("gameId", achievement.getGameId());
                    data.put("title", achievement.getTitle());
                    data.put("level", achievement.getLevel());
                    data.put("points", achievement.getPoints());
                    data.put("isCompleted", achievement.getIsCompleted());

                    batch.set(achievementRef, data, SetOptions.merge());
                }

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreSync", "Game data and achievements successfully synced");
                            runOnMainThread(() -> Toast.makeText(context, "Game data and achievements synced", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreSync", "Failed to sync game data and achievements", e);
                            runOnMainThread(() -> Toast.makeText(context, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
            } catch (Exception e) {
                Log.e("FirestoreSync", "Unexpected error during sync", e);
                runOnMainThread(() -> Toast.makeText(context, "Sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void syncStoryAchievementsWithFirestore() {
        executor.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null || firebaseUser.getEmail() == null) {
                    Log.w("FirestoreSync", "Firebase user or email is null");
                    return;
                }

                // Change to use the instance method
                List<StoryAchievementModel> achievements = storyAchievementDao.getAllStoryAchievements();
                if (achievements.isEmpty()) {
                    Log.w("FirestoreSync", "No story achievements found for user");
                    return;
                }

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                WriteBatch batch = firestore.batch();

                // Rest of the code remains the same
                for (StoryAchievementModel achievement : achievements) {
                    DocumentReference achievementRef = firestore.collection("storyachievements")
                            .document(firebaseUser.getEmail())
                            .collection("storiesdata")
                            .document("achievements_" + achievement.getId());

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", firebaseUser.getUid());
                    data.put("email", firebaseUser.getEmail());
                    data.put("storyId", achievement.getStoryId());
                    data.put("title", achievement.getTitle());
                    data.put("type", achievement.getType());
                    data.put("isCompleted", achievement.getIsCompleted());
                    data.put("count", achievement.getCount());

                    batch.set(achievementRef, data, SetOptions.merge());
                }

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreSync", "Story achievements successfully synced");
                            runOnMainThread(() -> Toast.makeText(context, "Story achievements synced", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreSync", "Failed to sync story achievements", e);
                            runOnMainThread(() -> Toast.makeText(context, "Story sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
            } catch (Exception e) {
                Log.e("FirestoreSync", "Unexpected error during story achievements sync", e);
                runOnMainThread(() -> Toast.makeText(context, "Story sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

 /*public void syncDataFromFirestore() {
        executor.execute(() -> {
            try {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null || firebaseUser.getEmail() == null) {
                    Log.w("FirestoreSync", "Firebase user or email is null");
                    return;
                }

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                String userEmail = firebaseUser.getEmail();

                // Sync FourPicsOneWord data
                firestore.collection("fourpicsoneword")
                        .document(userEmail)
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                FourPicsOneWord gameData = new FourPicsOneWord();
                                gameData.setEmail(document.getString("email"));
                                gameData.setUserId(Integer.parseInt(document.getString("userId")));
                                gameData.setCurrentLevel(document.getLong("currentLevel").intValue());
                                gameData.setPoints(document.getLong("points").intValue());
                                gameData.setDate(document.getString("date"));

                                executor.execute(() -> {
                                    fourPicsOneWordDao.insert(gameData);
                                    Log.d("FirestoreSync", "FourPicsOneWord data synced to SQLite");
                                });
                            }
                        });

                // Sync Game Achievements
                firestore.collection("gameachievements")
                        .document(userEmail)
                        .collection("gamesdata")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                if (doc.getId().startsWith("achievements_")) {
                                    try {
                                        GameAchievementModel achievement = new GameAchievementModel();
                                        achievement.setId(Integer.parseInt(doc.getId().replace("achievements_", "")));
                                        achievement.setGameId(doc.getString("gameId"));
                                        achievement.setTitle(doc.getString("title"));

                                        // Handle level field
                                        Object levelObj = doc.get("level");
                                        achievement.setLevel(convertToString(levelObj, "0"));

                                        // Handle points field
                                        Object pointsObj = doc.get("points");
                                        achievement.setPoints(convertToString(pointsObj, "0"));

                                        // Handle isCompleted field - using get() instead of getBoolean()
                                        Object isCompletedObj = doc.get("isCompleted");
                                        achievement.setIsCompleted(convertToString(isCompletedObj, "false"));

                                        executor.execute(() -> {
                                            gameAchievementDao.insert(achievement);
                                            Log.d("FirestoreSync", "Game achievement synced to SQLite: " + achievement.getId());
                                        });
                                    } catch (Exception e) {
                                        Log.e("FirestoreSync", "Error processing game achievement document: " + doc.getId(), e);
                                    }
                                }
                            }
                        });

                // Sync Story Achievements
                firestore.collection("storyachievements")
                        .document(userEmail)
                        .collection("storiesdata")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                if (doc.getId().startsWith("achievements_")) {
                                    try {
                                        StoryAchievementModel achievement = new StoryAchievementModel();
                                        achievement.setId(Integer.parseInt(doc.getId().replace("achievements_", "")));
                                        achievement.setStoryId(doc.getString("storyId"));
                                        achievement.setTitle(doc.getString("title"));
                                        achievement.setType(doc.getString("type"));

                                        // Handle isCompleted field - using get() instead of getBoolean()
                                        Object isCompletedObj = doc.get("isCompleted");
                                        achievement.setIsCompleted(convertToString(isCompletedObj, "false"));

                                        // Handle count field
                                        Object countObj = doc.get("count");
                                        if (countObj != null) {
                                            if (countObj instanceof Long) {
                                                achievement.setCount(((Long) countObj).intValue());
                                            } else if (countObj instanceof Integer) {
                                                achievement.setCount((Integer) countObj);
                                            } else {
                                                achievement.setCount(0);
                                            }
                                        } else {
                                            achievement.setCount(0);
                                        }

                                        executor.execute(() -> {
                                            storyAchievementDao.insert(achievement);
                                            Log.d("FirestoreSync", "Story achievement synced to SQLite: " + achievement.getId());
                                        });
                                    } catch (Exception e) {
                                        Log.e("FirestoreSync", "Error processing story achievement document: " + doc.getId(), e);
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreSync", "Error syncing data from Firestore", e);
                            runOnMainThread(() -> Toast.makeText(context,
                                    "Failed to sync data: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                        });

            } catch (Exception e) {
                Log.e("FirestoreSync", "Unexpected error during sync from Firestore", e);
                runOnMainThread(() -> Toast.makeText(context,
                        "Sync error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Helper method to convert various types to String
    private String convertToString(Object obj, String defaultValue) {
        if (obj == null) return defaultValue;

        if (obj instanceof Boolean) {
            return String.valueOf(obj);
        } else if (obj instanceof Long) {
            return String.valueOf(obj);
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Integer) {
            return String.valueOf(obj);
        }

        return defaultValue;
    }*/
    // Optional: Method to check Firebase Authentication
    public void checkFirebaseAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w("FirebaseAuth", "No authenticated user");
            return;
        }

        // Proceed with sync
        syncUserDataWithFirestore();
    }
}
