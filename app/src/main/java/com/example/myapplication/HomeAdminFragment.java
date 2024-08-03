package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.LoginUser;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeAdminFragment extends Fragment {

    private TextView userNameTextView, userRoleTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public HomeAdminFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.admin_dashboard, container, false);

        userNameTextView = view.findViewById(R.id.userName);
        userRoleTextView = view.findViewById(R.id.userRole);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            fetchAdminData(uid);
        } else {
            Toast.makeText(getActivity(), "No user is currently signed in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        return view;
    }



    private void fetchAdminData(String uid) {
        db.collection("admin").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String email = document.getString("email");
                        String firstName = document.getString("firstname");
                        String lastName = document.getString("lastname");
                        String role = document.getString("role");

                        String fullName;
                        if ((firstName == null || firstName.isEmpty()) && (lastName == null || lastName.isEmpty())) {
                            fullName = email != null ? email : "No Name";
                        } else {
                            fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        }

                        userNameTextView.setText(fullName.trim());
                        userRoleTextView.setText(role != null ? role : "N/A");

                        if (role != null && (role.equalsIgnoreCase("SuperAdmin") || role.equalsIgnoreCase("Admin"))) {
                            // Proceed with the activity as the user has the appropriate role
                        } else {
                            Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_SHORT).show();
                            redirectToLogin();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Admin data not found", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch admin data", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), LoginUser.class);
        startActivity(intent);
        getActivity().finish();
    }
}
