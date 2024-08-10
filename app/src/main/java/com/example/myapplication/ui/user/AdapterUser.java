package com.example.myapplication.ui.user;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUser> list;
    FirebaseAuth firebaseAuth;
    String uid;

    public AdapterUser(Context context, List<ModelUser> list) {
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

        try {
            Glide.with(context).load(userImage).into(holder.profiletv);
        } catch (Exception e) {
            // Handle the exception
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
                ModelUser user = list.get(position);

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

    private void showUserProfileDialog(ModelUser user) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
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
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showEditUserDialog(ModelUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        EditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        EditText etMiddleName = dialogView.findViewById(R.id.etMiddleName);
        EditText etLastName = dialogView.findViewById(R.id.etLastName);
        EditText etBirthday = dialogView.findViewById(R.id.etBirthday);
        Spinner spinnerGender = dialogView.findViewById(R.id.spinnerGender);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        ImageView ivProfileImage = dialogView.findViewById(R.id.ivProfileImage);
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
                R.array.roles_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        if (user.getRole() != null) {
            int spinnerPosition = roleAdapter.getPosition(user.getRole());
            spinnerRole.setSelection(spinnerPosition);
        }

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

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement image upload functionality here
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

                if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty() || gender.isEmpty() || email.isEmpty()) {
                    Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show();
                    return;
                }

                // Update user in Firestore
                updateUserInFirestore(user.getUid(), firstName, middleName, lastName, birthday, gender, role, email);
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

    private void showDatePicker(final EditText etBirthday) {
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

        new DatePickerDialog(context, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(EditText etBirthday, Calendar calendar) {
        String myFormat = "MM/dd/yyyy"; // Change the format as needed
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etBirthday.setText(sdf.format(calendar.getTime()));
    }

    private void updateUserInFirestore(String uid, String firstName, String middleName, String lastName, String birthday, String gender, String role, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Determine the collection based on the role
        String collectionName = role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("SuperAdmin") ? "admin" : "user";

        db.collection(collectionName).document(uid)
                .update("firstname", firstName,
                        "middlename", middleName,
                        "lastname", lastName,
                        "birthday", birthday,
                        "gender", gender,
                        "role", role,
                        "email", email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser(String userId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String collectionName = list.get(position).getRole().equalsIgnoreCase("Admin") ? "admin" : "user";

        // First, delete the user from Firestore
        db.collection(collectionName).document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Now, delete the user from Firebase Authentication
                        auth.getCurrentUser().delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Error deleting user from Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
