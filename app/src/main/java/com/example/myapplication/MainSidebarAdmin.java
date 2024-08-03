package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.navigation.NavigationView;

public class MainSidebarAdmin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.side_navmain);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fetch user data and set to nav header
        fetchUserData(navigationView);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeAdminFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void fetchUserData(NavigationView navigationView) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("admin").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String email = document.getString("email");
                            String firstName = document.getString("firstname");
                            String lastName = document.getString("lastname");

                            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                            View headerView = navigationView.getHeaderView(0);
                            TextView userNameTextView = headerView.findViewById(R.id.userName);
                            TextView userEmailTextView = headerView.findViewById(R.id.userEmail);

                            userNameTextView.setText(fullName.trim());
                            userEmailTextView.setText(email != null ? email : "No Email");
                        } else {
                            Toast.makeText(MainSidebarAdmin.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainSidebarAdmin.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainSidebarAdmin.this, LoginUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeAdminFragment()).commit();
        } else if (itemId == R.id.nav_user) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserAdminFragment()).commit();
        } else if (itemId == R.id.nav_content) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ContentAdminFragment()).commit();
        } else if (itemId == R.id.nav_analytics) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AnlyticsAdminFragment()).commit();
        } else if (itemId == R.id.nav_feedback) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedbackAdminFragment()).commit();
        } else if (itemId == R.id.nav_setting) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingAdminFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Sign out from Firebase
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(MainSidebarAdmin.this, "Logout!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainSidebarAdmin.this, LoginUser.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Finish current activity
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
