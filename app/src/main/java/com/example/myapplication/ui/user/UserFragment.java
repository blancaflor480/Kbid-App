package com.example.myapplication.ui.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterUser adapterUsers;
    private List<ModelUser> usersList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_admin, container, false);

        Spinner spinner = view.findViewById(R.id.user_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // Handle "All users" action
                        getAllUsers();
                        break;
                    case 1:
                        // Handle "User" action
                        getUsers();
                        break;
                    case 2:
                        // Handle "Admin" action
                        getAdmins();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        getAllUsers();

        // FloatingActionButton action
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });

        return view;
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        EditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        EditText etMiddleName = dialogView.findViewById(R.id.etMiddleName);
        EditText etLastName = dialogView.findViewById(R.id.etLastName);
        EditText etBirthday = dialogView.findViewById(R.id.etBirthday);
        EditText etGender = dialogView.findViewById(R.id.etGender);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Collect input data
                String firstName = etFirstName.getText().toString().trim();
                String middleName = etMiddleName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();
                String gender = etGender.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty() || gender.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(getView(), "Please fill in all required fields", Snackbar.LENGTH_LONG)
                            .setAnchorView(R.id.fab)
                            .show();
                    return;
                }

                // Add user to Firebase
                addUserToFirebase(firstName, middleName, lastName, birthday, gender, role, email, password);
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addUserToFirebase(String firstName, String middleName, String lastName, String birthday, String gender, String role, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // Create a new ModelUser instance with the provided data
                                ModelUser newUser = new ModelUser(
                                        firstName,
                                        middleName,
                                        lastName,
                                        email,
                                        null, // Assuming you are not setting an image
                                        uid,
                                        null, // Assuming you are not setting onlineStatus
                                        null, // Assuming you are not setting typingTo
                                        birthday,
                                        gender,
                                        role
                                );

                                // Save the new user to the appropriate Firestore collection based on the role
                                db.collection(role.toLowerCase()).document(uid)
                                        .set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(getView(), "User added successfully!", Snackbar.LENGTH_LONG)
                                                        .setAnchorView(R.id.fab)
                                                        .show();
                                                // Optionally, refresh the user list
                                                getAllUsers();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(getView(), "Error adding user: " + e.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAnchorView(R.id.fab)
                                                        .show();
                                            }
                                        });
                            }
                        } else {
                            Snackbar.make(getView(), "Error creating user: " + task.getException().getMessage(), Snackbar.LENGTH_LONG)
                                    .setAnchorView(R.id.fab)
                                    .show();
                        }
                    }
                });
    }

    private void getAllUsers() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        CollectionReference usersRef = db.collection("user");
        CollectionReference adminsRef = db.collection("admin");

        final List<ModelUser> combinedList = new ArrayList<>();

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle the error
                    return;
                }
                combinedList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    ModelUser modelUser = doc.toObject(ModelUser.class);
                    if (modelUser.getUid() != null && !modelUser.getUid().equals(firebaseUser.getUid())) {
                        combinedList.add(modelUser);
                    }
                }
                adminsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Handle the error
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            ModelUser modelUser = doc.toObject(ModelUser.class);
                            if (modelUser.getUid() != null && !modelUser.getUid().equals(firebaseUser.getUid())) {
                                combinedList.add(modelUser);
                            }
                        }
                        usersList.clear();
                        usersList.addAll(combinedList);
                        adapterUsers = new AdapterUser(getActivity(), usersList);
                        recyclerView.setAdapter(adapterUsers);
                    }
                });
            }
        });
    }

    private void getUsers() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        CollectionReference usersRef = db.collection("user");
        usersRef.whereEqualTo("role", "user").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle the error
                    return;
                }
                usersList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    ModelUser modelUsers = doc.toObject(ModelUser.class);
                    if (modelUsers.getUid() != null && !modelUsers.getUid().equals(firebaseUser.getUid())) {
                        usersList.add(modelUsers);
                    }
                }
                adapterUsers = new AdapterUser(getActivity(), usersList);
                recyclerView.setAdapter(adapterUsers);
            }
        });
    }

    private void getAdmins() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        CollectionReference usersRef = db.collection("admin");
        usersRef.whereIn("role", Arrays.asList("Admin", "SuperAdmin"))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Handle the error
                            return;
                        }
                        usersList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            ModelUser modelUsers = doc.toObject(ModelUser.class);
                            if (modelUsers.getUid() != null && !modelUsers.getUid().equals(firebaseUser.getUid())) {
                                usersList.add(modelUsers);
                            }
                        }
                        adapterUsers = new AdapterUser(getActivity(), usersList);
                        recyclerView.setAdapter(adapterUsers);
                    }
                });
    }
}
