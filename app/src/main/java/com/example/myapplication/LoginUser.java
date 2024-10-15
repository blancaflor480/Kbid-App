package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUser extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonSignin, buttonCreate;
    private RelativeLayout googlesignIn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader, noInternet;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignin = findViewById(R.id.buttonSignin);
        googlesignIn = findViewById(R.id.googlesignIn);
        buttonCreate = findViewById(R.id.buttonCreate);
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);

        googlesignIn.setOnClickListener(v -> signInWithGoogle());

        buttonSignin.setOnClickListener(v -> loginUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, SignupUser.class)));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isConnected()) {
            showNoInternet();
            return;
        }

        // Show loader and hide the button when login is initiated
        showLoader();

        // Authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestore(user.getUid());
                        }
                    } else {
                        // If sign in fails, hide loader and show message
                        hideLoader();
                        Toast.makeText(LoginUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(String userId) {
        firestore.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            navigateToHome();
                        } else {
                            firestore.collection("admin").document(userId).get()
                                    .addOnCompleteListener(adminTask -> {
                                        if (adminTask.isSuccessful()) {
                                            DocumentSnapshot adminDocument = adminTask.getResult();
                                            if (adminDocument.exists()) {
                                                String role = adminDocument.getString("role");
                                                if (role != null && (role.equals("Admin") || role.equals("SuperAdmin"))) {
                                                    navigateToAdminDashboard();
                                                } else {
                                                    showNoAccessMessage();
                                                }
                                            } else {
                                                handleInvalidAccount(); // Call to sign out and select a new account
                                            }
                                        } else {
                                            Log.e("LoginUser", "Error checking admin collection", adminTask.getException());
                                        }
                                        hideLoader();  // Hide loader once done
                                    });
                        }
                    } else {
                        Log.e("LoginUser", "Error checking user collection", task.getException());
                        hideLoader();  // Hide loader on error
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
                            checkUserInFirestore(user.getUid());
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
