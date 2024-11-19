package com.example.myapplication;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {

    private EditText inputEmail;
    private MaterialButton submitButton;
    private LottieAnimationView loader, noInternet;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Bind UI components
        inputEmail = findViewById(R.id.inputEmail);
        submitButton = findViewById(R.id.Submit);
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);

        // Handle Submit button click
        submitButton.setOnClickListener(v -> {
            if (!isConnected()) {
                showNoInternet();
                return;
            }
            resetPassword();
        });
    }

    /**
     * Handles the password reset process.
     */
    private void resetPassword() {
        String email = inputEmail.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            showValidationDialog("Please enter your registered email.");
            return;
        }

        // Show loader while processing
        showLoader();

        // Send password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideLoader();
                    if (task.isSuccessful()) {
                        showValidationDialog("A password reset email has been sent. Please check your inbox.");
                    } else {
                        showValidationDialog("Failed to send reset email. Please try again later.");
                    }
                });
    }

    private void showValidationDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sucessful, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView messageTextView = dialogView.findViewById(R.id.message);
        messageTextView.setText(message);

        Button buttonOk = dialogView.findViewById(R.id.submit);
        buttonOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Checks network connectivity.
     */
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Displays the loader animation and hides other UI elements.
     */
    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
    }

    /**
     * Hides the loader animation and shows the submit button.
     */
    private void hideLoader() {
        loader.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the no-internet animation.
     */
    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        showToast("No internet connection.");
    }

    /**
     * Utility method to display a short toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
