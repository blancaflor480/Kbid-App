package com.example.myapplication.ui.logout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.LoginUser;
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Return the layout for this fragment
        return inflater.inflate(R.layout.fragment_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_side_navigation_admin);

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Return to the home screen using NavController
                        navController.navigate(R.id.nav_home);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Also sign out from GoogleSignInClient
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Show success message
            View view = getView();
            if (view != null) {
                TextView successMessage = view.findViewById(R.id.logout_message);
                successMessage.setVisibility(View.VISIBLE);
            }

            // Optionally, use Toast to show the message
            Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();

            // Redirect to LoginUser activity after a short delay
            if (getActivity() != null) {
                view.postDelayed(() -> {
                    Intent intent = new Intent(getActivity().getApplicationContext(), LoginUser.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish(); // Optional: Finish the current activity
                }, 1000); // Delay for 1 second to allow the user to see the success message
            }
        });
    }

}
