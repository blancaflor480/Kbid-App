package com.example.myapplication.ui.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterAdmin extends RecyclerView.Adapter<AdapterAdmin.MyHolder> {

    Context context;
    List<ModelAdmin> list;
    FirebaseAuth firebaseAuth;
    String uid;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView ivProfileImage;
    private int RESULT_OK;

    public AdapterAdmin(Context context, List<ModelAdmin> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }




    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisuid = list.get(position).getUid();
        String userImage = list.get(position).getImageUrl();
        String username = list.get(position).getName();
        String usermail = list.get(position).getEmail();
        String role = list.get(position).getRole();

        holder.name.setText(username);
        holder.email.setText(usermail);
        holder.role.setText(role);

        // Check if the user image URL is null or empty
        if (userImage != null && !userImage.isEmpty()) {
            try {
                Glide.with(context).load(userImage).into(holder.profiletv);
            } catch (Exception e) {
                // Handle the exception (optional: log the error)
                holder.profiletv.setImageResource(R.drawable.user); // Set default image in case of error
            }
        } else {
            // Set default image if no image URL is provided
            holder.profiletv.setImageResource(R.drawable.user);
        }


        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                ModelAdmin user = list.get(position);

                if (itemId == R.id.Viewprof) {
                    // Handle view profile action
                    showUserProfileDialog(user);
                    return true;
                } else if (itemId == R.id.Editprof) {
                    // Handle edit profile action
                    showEditUserDialog(user);
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    // Handle delete profile action
                    deleteUser(user.getUid(), position);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void showUserProfileDialog(ModelAdmin user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_view_user, null);
        builder.setView(dialogView);

        // Initialize views
        CircleImageView profileImageView = dialogView.findViewById(R.id.profileImageView);
        TextView firstNameTextView = dialogView.findViewById(R.id.firstNameTextView);
        TextView middleNameTextView = dialogView.findViewById(R.id.middleNameTextView);
        TextView lastNameTextView = dialogView.findViewById(R.id.lastNameTextView);
        TextView birthdayTextView = dialogView.findViewById(R.id.birthdayTextView);
        TextView genderTextView = dialogView.findViewById(R.id.genderTextView);
        TextView roleTextView = dialogView.findViewById(R.id.roleTextView);
        TextView emailTextView = dialogView.findViewById(R.id.emailTextView);

        // Populate user data
        firstNameTextView.setText("First Name: " + user.getFirstname());
        middleNameTextView.setText("Middle Name: " + user.getMiddlename());
        lastNameTextView.setText("Last Name: " + user.getLastname());
        birthdayTextView.setText("Birthday: " + user.getBirthday());
        genderTextView.setText("Gender: " + user.getGender());
        roleTextView.setText("Role: " + user.getRole());
        emailTextView.setText("Email: " + user.getEmail());

        // Load profile image
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(context).load(user.getImageUrl()).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.user); // Replace with your default image
        }

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditUserDialog(ModelAdmin user) {
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        // Fragment management: Attach ImageFragment to the activity
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("IMAGE_FRAGMENT");

        if (imageFragment == null) {
            imageFragment = new ImageFragment();
            fragmentManager.beginTransaction().add(imageFragment, "IMAGE_FRAGMENT").commit();
            fragmentManager.executePendingTransactions(); // Ensure the transaction is complete
        }

        EditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        EditText etMiddleName = dialogView.findViewById(R.id.etMiddleName);
        EditText etLastName = dialogView.findViewById(R.id.etLastName);
        EditText etBirthday = dialogView.findViewById(R.id.etBirthday);
        Spinner spinnerGender = dialogView.findViewById(R.id.spinnerGender);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        ivProfileImage = dialogView.findViewById(R.id.ivProfileImage);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);

        // Populate fields with current user data
        etFirstName.setText(user.getFirstname());
        etMiddleName.setText(user.getMiddlename());
        etLastName.setText(user.getLastname());
        etBirthday.setText(user.getBirthday());
        etEmail.setText(user.getEmail());

        // Set up the gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(context,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        if (user.getGender() != null) {
            int spinnerPosition = genderAdapter.getPosition(user.getGender());
            spinnerGender.setSelection(spinnerPosition);
        }

        // Set up the role spinner
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(context,
                R.array.admin_roles_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        if (user.getRole() != null) {
            int spinnerPosition = roleAdapter.getPosition(user.getRole());
            spinnerRole.setSelection(spinnerPosition);
        }
        // Set the listener for image selection
        imageFragment.setOnImageSelectedListener(new ImageFragment.OnImageSelectedListener() {
            @Override
            public void onImageSelected(Uri imageUri) {
                updateImageUri(imageUri);
            }
        });

        // Set profile image if available
        if (user.getImageUrl() != null) {
            Glide.with(context).load(user.getImageUrl()).into(ivProfileImage);
        }

        // Set up DatePicker for birthday
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etBirthday);
            }
        });


        ImageFragment finalImageFragment = imageFragment;
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the file chooser in the fragment
                if (finalImageFragment.isAdded() && !finalImageFragment.isDetached()) {
                    finalImageFragment.openFileChooser();
                } else {
                    Toast.makeText(context, "Image picker not available. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Collect updated data
                String firstName = etFirstName.getText().toString().trim();
                String middleName = etMiddleName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();
                String gender = spinnerGender.getSelectedItem().toString();
                String role = spinnerRole.getSelectedItem().toString();
                String email = etEmail.getText().toString().trim();

                // Validate input
                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                    Toast.makeText(context, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String collection = user.getRole().equals("Teacher") ? "admin" : "user";
                updateUser(collection,
                        user.getUid(),
                        firstName,
                        middleName,
                        lastName,
                        birthday,
                        gender,
                        role,
                        email,
                        imageUri);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void showDatePicker(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        editText.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(context, "Context is not an Activity", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateImageUri(Uri imageUrl) {
        this.imageUri = imageUrl;
        if (ivProfileImage != null) {
            ivProfileImage.setImageURI(imageUri); // Update ImageView with the new image URI
        }
    }

    private void updateUser(String collection, String uid, String firstName, String middleName, String lastName,
                            String birthday, String gender, String role, String email, Uri imageUri) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Update Firestore fields
        db.collection(collection).document(uid)
                .update(
                        "firstname", firstName,
                        "middlename", middleName,
                        "lastname", lastName,
                        "birthday", birthday,
                        "gender", gender,
                        "role", role,
                        "email", email,
                        "imageUrl", imageUri
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show();

                    // Handle profile image update if a new image is selected
                    if (imageUri != null) {
                        // Generate a unique filename using the UID to avoid caching issues
                        String imageFileName = "Profile_image/" + uid + ".jpg";
                        StorageReference imageRef = storageRef.child(imageFileName);

                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    Log.d("ImageUploadSuccess", "New Image URL: " + downloadUrl);

                                    // Update Firestore with the new image URL
                                    db.collection(collection).document(uid)
                                            .update("imageUrl", downloadUrl)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Log.d("FirestoreUpdate", "Firestore image URL updated successfully.");
                                                // Update the user list and notify the adapter
                                                for (ModelAdmin user : list) {
                                                    if (user.getUid().equals(uid)) {
                                                        user.setImageUrl(downloadUrl);
                                                        notifyDataSetChanged();
                                                        break;
                                                    }
                                                }
                                                Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirestoreError", "Failed to update Firestore image URL: " + e.getMessage());
                                                Toast.makeText(context, "Failed to update profile image URL in Firestore.", Toast.LENGTH_SHORT).show();
                                            });
                                }))
                                .addOnFailureListener(e -> {
                                    Log.e("ImageUploadError", "Error uploading image: " + e.getMessage());
                                    Toast.makeText(context, "Failed to upload profile image.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d("ImageUpload", "No new image selected, skipping image upload.");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show());
    }




    private void deleteUser(String userId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(context, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getUid().equals(userId)) {
            Toast.makeText(context, "You cannot delete your own account while logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the collection name based on user role
        String collectionName = list.get(position).getRole().equalsIgnoreCase("Admin") || list.get(position).getRole().equalsIgnoreCase("SuperAdmin") ? "admin" : "user";

        // Reauthenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), "password"); // Replace "user-password" with the user's password
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Delete the user from Firestore
                    db.collection(collectionName).document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                // Delete the user from Firebase Authentication
                                currentUser.delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(context, "User deleted successfully from Firestore and Authentication", Toast.LENGTH_SHORT).show();

                                            // Safely remove the item from the list
                                            if (position >= 0 && position < list.size()) {
                                                list.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, list.size());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Detailed error message for Authentication deletion failure
                                            Log.e("DeleteUser", "Error deleting user from Authentication: ", e);
                                            Toast.makeText(context, "Error deleting user from Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // Detailed error message for Firestore deletion failure
                                Log.e("DeleteUser", "Error deleting user from Firestore: ", e);
                                Toast.makeText(context, "Error deleting user from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Detailed error message for reauthentication failure
                    Log.e("DeleteUser", "Error reauthenticating user: ", e);
                    Toast.makeText(context, "Error reauthenticating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name, email, role;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagep);
            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
            role = itemView.findViewById(R.id.rolep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}