package com.example.myapplication.ui.admin;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.user.AdapterUser;
import com.example.myapplication.ui.user.ModelUser;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView ivProfileImage;
    private StorageReference storageRef;
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterAdmin adapterUsers;
    private List<ModelAdmin> usersList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_admin, container, false);

        Spinner spinner = view.findViewById(R.id.user_dropdown);
        SearchView searchBar = view.findViewById(R.id.search_bar);
        notFoundTextView = view.findViewById(R.id.not_found_message);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.admin_options_array, android.R.layout.simple_spinner_item);
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
                        getTeacher();
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
        storageRef = FirebaseStorage.getInstance().getReference(); // Initialize storageRef here
        getAllUsers();

        // FloatingActionButton action
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });



        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the query is submitted
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as the query text changes
                filterUsers(newText);
                return false;
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
        Spinner spinnerGender = dialogView.findViewById(R.id.spinnerGender);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        ivProfileImage = dialogView.findViewById(R.id.ivProfileImage);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);

        // Set up the gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender_array, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.admin_roles_array, R.layout.spinner_item);
        roleAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerRole.setAdapter(roleAdapter);

        // Set up date picker
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etBirthday);
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Collect input data
                String firstName = etFirstName.getText().toString().trim();
                String middleName = etMiddleName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();
                String gender = spinnerGender.getSelectedItem().toString();
                String role = spinnerRole.getSelectedItem().toString();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty() || gender.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(getView(), "Please fill in all required fields", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                // Create new user
                createNewUser(firstName, middleName, lastName, birthday, gender, role, email, password);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePicker(EditText etBirthday) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(etBirthday, calendar);
            }
        };

        new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(EditText etBirthday, Calendar calendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etBirthday.setText(sdf.format(calendar.getTime()));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
        }
    }

    private void createNewUser(String firstName, String middleName, String lastName, String birthday, String gender, String role, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                if (imageUri != null) {
                                    // Upload profile image if available
                                    StorageReference fileReference = storageRef.child("Profile_Image/" + uid + "." + getFileExtension(imageUri));
                                    fileReference.putFile(imageUri)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String imageUrl = uri.toString();
                                                            // Create a new ModelAdmin instance with image URL
                                                            ModelAdmin newUser = new ModelAdmin(
                                                                    firstName,
                                                                    middleName,
                                                                    lastName,
                                                                    email,
                                                                    imageUrl,
                                                                    uid,
                                                                    null,
                                                                    null,
                                                                    birthday,
                                                                    gender,
                                                                    role
                                                            );
                                                            saveUserToFirestore(newUser, role, uid);
                                                        }
                                                    });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Snackbar.make(getView(), "Failed to upload image: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    // Create a new ModelAdmin instance without an image URL
                                    ModelAdmin newUser = new ModelAdmin(
                                            firstName,
                                            middleName,
                                            lastName,
                                            email,
                                            null, // No image URL
                                            uid,
                                            null,
                                            null,
                                            birthday,
                                            gender,
                                            role
                                    );
                                    saveUserToFirestore(newUser, role, uid);
                                }
                            }
                        } else {
                            Snackbar.make(getView(), "Error creating user: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }



    private void saveUserToFirestore(ModelAdmin newUser, String role, String uid) {
        // Determine the collection based on the role
        String collectionName;
        if (role.equalsIgnoreCase("Teacher") || role.equalsIgnoreCase("SuperAdmin")) {
            collectionName = "admin";
        } else {
            collectionName = "admin"; // Default collection for other roles
        }

        db.collection(collectionName).document(uid)
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(getView(), "User added successfully!", Snackbar.LENGTH_LONG).show();
                        getAllUsers();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), "Error adding user: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }



    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void getAllUsers() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        CollectionReference adminsRef = db.collection("admin");

        final Set<String> uniqueAdminIds = new HashSet<>(); // Use a Set to track unique admin IDs
        final List<ModelAdmin> combinedList = new ArrayList<>();

        adminsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle the error
                return;
            }

            combinedList.clear();
            uniqueAdminIds.clear(); // Clear the set before populating

            for (QueryDocumentSnapshot doc : value) {
                ModelAdmin modelAdmin = doc.toObject(ModelAdmin.class);

                // Check if admin is not the current user and not already added
                if (modelAdmin.getUid() != null
                        && !modelAdmin.getUid().equals(firebaseUser.getUid())
                        && !uniqueAdminIds.contains(modelAdmin.getUid())) {

                    combinedList.add(modelAdmin);
                    uniqueAdminIds.add(modelAdmin.getUid()); // Track added admin IDs
                }
            }

            usersList.clear();
            usersList.addAll(combinedList);
            adapterUsers = new AdapterAdmin(getActivity(), usersList);
            recyclerView.setAdapter(adapterUsers);
        });
    }

    private void getTeacher() {
        CollectionReference usersRef = db.collection("admin");

        // Filter to get only documents where role is "Teacher"
        usersRef.whereEqualTo("role", "Teacher").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return; // Handle the error if needed
                }
                usersList.clear();
                for (QueryDocumentSnapshot document : value) {
                    ModelAdmin user = document.toObject(ModelAdmin.class);
                    usersList.add(user);
                }
                // Update the adapter with the filtered list
                adapterUsers = new AdapterAdmin(getActivity(), usersList);
                recyclerView.setAdapter(adapterUsers);
            }
        });
    }

    private void getAdmins() {
        CollectionReference adminsRef = db.collection("admin");

        // Filter to get only documents where role is "SuperAdmin"
        adminsRef.whereEqualTo("role", "SuperAdmin").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return; // Handle the error if needed
                }
                usersList.clear();
                for (QueryDocumentSnapshot document : value) {
                    ModelAdmin admin = document.toObject(ModelAdmin.class);
                    usersList.add(admin);
                }
                // Update the adapter with the filtered list
                adapterUsers = new AdapterAdmin(getActivity(), usersList);
                recyclerView.setAdapter(adapterUsers);
            }
        });
    }




    @Override
    public void onResume() {
        super.onResume();
        // Reset the "Not Found" message and display the full user list
        notFoundTextView.setVisibility(View.GONE);
        adapterUsers = new AdapterAdmin(getContext(), usersList);
        recyclerView.setAdapter(adapterUsers);
    }


    private void filterUsers(String query) {
        List<ModelAdmin> filteredList = new ArrayList<>();
        for (ModelAdmin user : usersList) {
            String firstname = user.getFirstname() != null ? user.getFirstname().toLowerCase() : "";
            String lastname = user.getLastname() != null ? user.getLastname().toLowerCase() : "";
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

            if (firstname.contains(query.toLowerCase()) ||
                    lastname.contains(query.toLowerCase()) ||
                    email.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Update the RecyclerView and the "Not Found" message
        if (filteredList.isEmpty()) {
            notFoundTextView.setVisibility(View.VISIBLE);
        } else {
            notFoundTextView.setVisibility(View.GONE);
        }

        adapterUsers = new AdapterAdmin(getContext(), filteredList);
        recyclerView.setAdapter(adapterUsers);
    }
}
