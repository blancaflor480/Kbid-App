package com.example.myapplication;

import static com.example.myapplication.R.id.nav_setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.databinding.SideNavigationAdminBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.appcompat.app.AlertDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class SideNavigationAdmin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private SideNavigationAdminBinding binding;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = SideNavigationAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarSideNavigationAdmin.toolbar;
        setSupportActionBar(toolbar);



        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fetch user data and set to nav header
        fetchUserData(navigationView);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_user, R.id.nav_content, R.id.nav_record, R.id.nav_feedback, R.id.nav_setting)
                .setOpenableLayout(drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_side_navigation_admin);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
                            String imageUrl = document.getString("imageUrl");

                            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                            View headerView = navigationView.getHeaderView(0);
                            TextView userNameTextView = headerView.findViewById(R.id.userName);
                            TextView userEmailTextView = headerView.findViewById(R.id.userEmail);
                            CircleImageView userImageView = headerView.findViewById(R.id.imageView);
                            userNameTextView.setText(fullName.trim());
                            userEmailTextView.setText(email != null ? email : "No Email");

                            // Load the profile image using Glide or show a default image
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(SideNavigationAdmin.this)
                                        .load(imageUrl)
                                        .placeholder(R.mipmap.ic_launcher_round) // Placeholder image
                                        .error(R.mipmap.ic_launcher_round) // Default image in case of error
                                        .into(userImageView);
                            } else {
                                // Show default image if no imageUrl is available
                                userImageView.setImageResource(R.mipmap.ic_launcher_round);
                            }
                        } else {
                            Toast.makeText(SideNavigationAdmin.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SideNavigationAdmin.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }



    private void redirectToLogin() {
        Intent intent = new Intent(SideNavigationAdmin.this, LoginUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_logout) {
            // Navigate to LogoutFragment
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_side_navigation_admin);
            navController.navigate(R.id.nav_logout);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // Handle navigation for other items
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_side_navigation_admin);
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

        drawerLayout.closeDrawer(GravityCompat.START);
        return handled;
    }


    // @Override
    //  public boolean onCreateOptionsMenu(Menu menu) {
    //     getMenuInflater().inflate(R.menu.side_navigation_admin, menu);
    //     return true;
    //  }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_side_navigation_admin);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
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
