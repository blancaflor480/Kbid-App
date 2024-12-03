package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginUser extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private TextView forgottedpassword;
    private Button buttonCreate;
    private MaterialButton buttonSignin;
    private RelativeLayout googlesignIn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader, noInternet;
    private static final int RC_SIGN_IN = 9001;
    private UserDatabaseHelper userDatabaseHelper;
    private AuthSyncHelper authSyncHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);
        authSyncHelper = new AuthSyncHelper(this);
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userDatabaseHelper = new UserDatabaseHelper(this);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        forgottedpassword = findViewById(R.id.forgottedpassword);
        buttonSignin = findViewById(R.id.buttonSignin);
        googlesignIn = findViewById(R.id.googlesignIn);
        buttonCreate = findViewById(R.id.buttonCreate);
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);

        googlesignIn.setOnClickListener(v -> signInWithGoogle());

        buttonSignin.setOnClickListener(v -> loginUser());
        forgottedpassword.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, ForgetPassword.class)));
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, SignupUser.class)));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void loginUser() {
        String emailOrControlId = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrControlId)) {
            showValidationDialog("Enter email or control ID!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showValidationDialog("Enter password!");
            return;
        }

        if (!isConnected()) {
            showNoInternet();
            return;
        }

        // Show loader and hide the button when login is initiated
        showLoader();

        // Check if input is email or Control ID
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrControlId).matches()) {
            // Input is an email
            loginWithEmail(emailOrControlId, password);
        } else {
            // Input is a Control ID
            firestore.collection("user")
                    .whereEqualTo("controlid", emailOrControlId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String email = document.getString("email");

                            if (email != null) {
                                loginWithEmail(email, password);
                            } else {
                                hideLoader();
                                showValidationDialog("No account associated with this Control ID.");
                            }
                        } else {
                            hideLoader();
                            showValidationDialog("Invalid Control ID. Please try again.");
                        }
                    });
        }
    }

    private void loginWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    if (user.isEmailVerified()) {
                                        // Proceed with Firestore check if email is verified
                                        checkUserInFirestore(user.getUid(), user.getEmail());
                                    } else {
                                        hideLoader();
                                        // Show message prompting user to verify email
                                        showValidationDialog("Need to verify your account. Please check your email for the verification link.");
                                        // Optionally, send a verification email if not already sent
                                        user.sendEmailVerification()
                                                .addOnSuccessListener(aVoid ->
                                                        Toast.makeText(LoginUser.this,
                                                                "Verification email sent. Please check your email.",
                                                                Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e ->
                                                        Log.e("Verification", "Failed to send verification email", e));
                                    }
                                } else {
                                    hideLoader();
                                    showValidationDialog("Failed to verify account. Please try again.");
                                }
                            });
                        }
                    } else {
                        // If sign in fails, hide loader and show message
                        hideLoader();
                        showValidationDialog("Incorrect email or password. Please try again.");
                    }
                });
    }


    private void showValidationDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginUser.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_authenticateaccount, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView messageTextView = dialogView.findViewById(R.id.message);
        messageTextView.setText(message);

        Button buttonOk = dialogView.findViewById(R.id.submit);
        buttonOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkUserInFirestore(String userId, String email) {
        showLoader();
        FirebaseUser currentUser = auth.getCurrentUser();

        authSyncHelper.syncUserDataFromFirestore(currentUser, new AuthSyncHelper.SyncCallback() {
            @Override
            public void onSuccess(String userType) {

                if (userType.equals("user") || userType.equals("incomplete_profile")) {
                    // Get userId from local database
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        UserDao userDao = AppDatabase.getDatabase(LoginUser.this).userDao();
                        User user = userDao.getUserByEmail(email);
                        if (user != null) {
                            long localUserId = user.getId();
                            // Sync both achievements
                            authSyncHelper.syncGameAchievements(email, localUserId);
                            Log.d("LoginUser", "Syncing story achievements for email: " + email);
                            Log.d("LoginUser", "Local user ID: " + localUserId);
                            authSyncHelper.syncStoryAchievements(email, localUserId);
                        }
                    });
                }


                if (userType.equals("admin")) {
                    // If admin, go directly to admin dashboard
                    navigateToAdminDashboard();
                    return;
                } else if (userType.equals("invalid")) {
                    handleInvalidAccount();
                    return;
                }

                firestore.collection("user")
                        .document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();

                                // Check for avatar-related fields
                                String avatarName = document.getString("avatarName");
                                String firstname = document.getString("firstname");
                                String birthday = document.getString("birthday");
                                String controlId = document.getString("controlid");

                                hideLoader();

                                // Conditions for navigation
                                if (TextUtils.isEmpty(avatarName)) {
                                    // No avatar set, go to Avatar Activity
                                    navigateToAvatarActivity();
                                } else if (TextUtils.isEmpty(firstname) ||
                                        TextUtils.isEmpty(birthday) ||
                                        TextUtils.isEmpty(controlId)) {
                                    // Incomplete profile, go to Child Name Activity
                                    navigateToChildname();
                                } else {
                                    // Full profile exists, navigate based on user type
                                    navigateToHome();
                                }
                            } else {
                                hideLoader();
                                showValidationDialog("Error fetching user data");
                            }
                        });
            }

            @Override
            public void onError(String message) {
                hideLoader();
                showValidationDialog(message);
            }
        });
    }



    //Google SignIn
    private void signInWithGoogle() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // You'll get this from google-services.json
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleInvalidAccount() {
        // Sign out from Firebase Auth
        auth.signOut();

        // Get the GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sign out from Google Sign-In
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Once signed out, display a message and prompt the user to select another account
            Toast.makeText(LoginUser.this, "The selected account does not exist. Please choose another account.", Toast.LENGTH_SHORT).show();

            // Reopen the Google sign-in prompt
            signInWithGoogle();  // Trigger Google sign-in again
        });
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        // Also sign out from GoogleSignInClient
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                String idToken = account.getIdToken();
                authenticateWithFirebase(idToken);
            }
        } catch (ApiException e) {
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Failed to sign in. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticateWithFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestore(user.getUid(), user.getEmail());
                        }
                    } else {
                        Toast.makeText(LoginUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void showNoAccountExists() {
        hideLoader();
        Toast.makeText(LoginUser.this, "Your account does not exist.", Toast.LENGTH_SHORT).show();
    }

    private void navigateToChildname() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginUser.this, ChildNameActivity.class);
            intent.putExtra("USER_EMAIL", user.getEmail()); // Pass the verified email

            // If control ID was used for login, get control ID from Firestore and pass it
            firestore.collection("user")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String controlId = document.getString("controlid");
                                if (controlId != null) {
                                    intent.putExtra("CONTROL_ID", controlId);
                                }
                            }
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }

    private void navigateToAvatarActivity() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("user")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            Intent intent = new Intent(LoginUser.this, AvatarActivity.class);

                            // Add all required data as extras
                            intent.putExtra("CONTROL_ID", document.getString("controlid"));
                            intent.putExtra("USER_EMAIL", user.getEmail());
                            intent.putExtra("FIRSTNAME", document.getString("firstname"));
                            intent.putExtra("BIRTHDAY", document.getString("birthday"));
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish(); // Close this activity
                        } else {
                            hideLoader();
                            showValidationDialog("Error fetching user data");
                        }
                    });
        }
    }

    private void navigateToHome() {
        hideLoader();  // Hide the loader when navigating
        Intent intent = new Intent(LoginUser.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void navigateToAdminDashboard() {
        hideLoader();  // Hide the loader when navigating
        Intent intent = new Intent(LoginUser.this, SideNavigationAdmin.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void showNoAccessMessage() {
        hideLoader();  // Hide loader
        Toast.makeText(LoginUser.this, "No access rights.", Toast.LENGTH_SHORT).show();
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
        buttonSignin.setVisibility(View.GONE); // Hide the button when loader is visible

        // Optionally, apply a blur effect or other UI changes if needed
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
        buttonSignin.setVisibility(View.VISIBLE); // Show the button again if loader is hidden
        noInternet.setVisibility(View.GONE);
    }

    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        buttonSignin.setVisibility(View.VISIBLE); // Ensure button is visible if there's no internet
    }
}
