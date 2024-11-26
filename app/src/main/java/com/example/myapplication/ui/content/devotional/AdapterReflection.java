package com.example.myapplication.ui.content.devotional;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.admin.ModelAdmin;
import com.example.myapplication.ui.content.stories.ModelStories;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterReflection extends RecyclerView.Adapter<AdapterReflection.MyHolder> {

    private final Context context;
    private final List<ModelReflection> list;

    public AdapterReflection(Context context, List<ModelReflection> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_reflectionkids, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelReflection reflection = list.get(position);

        // Bind data to the views
        holder.emailTextView.setText(reflection.getEmail());
        holder.reflectionanswer.setText(reflection.getReflectionanswer());
        String id = reflection.getId();

        // Format and display the timestamp
        Date timestamp = reflection.getTimestamp();
        holder.timestampTextView.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(timestamp) : "N/A");

        // Load image (if available) or show a placeholder
        String imageUrl = reflection.getImageUrl(); // Assuming `ModelReflection` has `getImageUrl()`.
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.userkids) // Replace with your placeholder drawable
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.userkids);
        }

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position, id); // Pass the storyId here
            }
        });
    }

    private void showPopupMenu(View view, int position, String storyId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_devotional, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ModelReflection reflection = list.get(position);
                int itemId = item.getItemId();

                if (itemId == R.id.Viewprof) {
                    showAnswerDialog(reflection);
                    return true;
                } else if (itemId == R.id.Feedback) {
                    showFeedbackDialog(reflection);
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Reflection")
                            .setMessage("Are you sure you want to delete this reflection?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // Perform deletion
                                deleteReflection(reflection, position);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void showAnswerDialog(ModelReflection reflection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_viewanswer, null);
        builder.setView(dialogView);

        // Initialize views
        CircleImageView profileImageView = dialogView.findViewById(R.id.profileImageView);
        TextView controlId = dialogView.findViewById(R.id.controlid);
        TextView refelectionAnswer = dialogView.findViewById(R.id.reflectionanswer);
        TextView devotionalId = dialogView.findViewById(R.id.devotelid);
        TextView emailTextView = dialogView.findViewById(R.id.emailTextView);

        // Populate user data
        devotionalId.setText("Devotional ID: " + reflection.getId());
        controlId.setText("Control ID user: " + reflection.getControlId());
        emailTextView.setText("Email: " + reflection.getEmail());
        refelectionAnswer.setText("Reflection Answer: " + reflection.getReflectionanswer());

        // Load profile image
        if (reflection.getImageUrl() != null && !reflection.getImageUrl().isEmpty()) {
            Glide.with(context).load(reflection.getImageUrl()).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.user); // Replace with your default image
        }

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showFeedbackDialog(ModelReflection reflection) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_feedback, null);

        // Initialize views in the custom layout
        TextView emailTextView = dialogView.findViewById(R.id.email);
        TextView memoryVerseTextView = dialogView.findViewById(R.id.memoryverse);
        TextView verseTextView = dialogView.findViewById(R.id.verse);
        TextView dateDailyTextView = dialogView.findViewById(R.id.datedaily);
        Spinner badgeSpinner = dialogView.findViewById(R.id.spinnerBadge);
        EditText feedbackEditText = dialogView.findViewById(R.id.feedback);
        ImageButton closeButton = dialogView.findViewById(R.id.close);
        Button submitButton = dialogView.findViewById(R.id.btnSubmit);

        // Populate data
        emailTextView.setText(reflection.getEmail());
        memoryVerseTextView.setText(reflection.getReflectionanswer());
        verseTextView.setText(reflection.getVerse());
        feedbackEditText.setText(reflection.getFeedback());
        badgeSpinner.setSelection(reflection.getBadge().equals("Star Thinker") ? 0 :
                reflection.getBadge().equals("Creative Contributor") ? 1 : 2);
        dateDailyTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(reflection.getTimestamp()));

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item,
                new String[]{"Star Thinker", "Creative Contributor", "Consistent Reflector"});
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        badgeSpinner.setAdapter(adapter);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Close button action
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Submit button action
        submitButton.setOnClickListener(v -> {
            String feedback = feedbackEditText.getText().toString().trim();
            String selectedBadge = badgeSpinner.getSelectedItem().toString();

            // Validate feedback input
            if (feedback.isEmpty()) {
                Toast.makeText(context, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore reference to find the specific document
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("kidsReflection")
                    .whereEqualTo("email", reflection.getEmail())
                    .whereEqualTo("controlId", reflection.getControlId())
                    .whereEqualTo("timestamp", reflection.getTimestamp())
                    .whereEqualTo("reflectionanswer", reflection.getReflectionanswer())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first matching document (assuming unique combination)
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            // Prepare data to update
                            Map<String, Object> update = new HashMap<>();
                            update.put("feedback", feedback);
                            update.put("badge", selectedBadge);

                            // Update the specific document
                            documentSnapshot.getReference().update(update)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();

                                        // Update the local model to reflect changes
                                        reflection.setFeedback(feedback);
                                        reflection.setBadge(selectedBadge);

                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to submit feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "No matching reflection found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to find reflection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Show the dialog
        dialog.show();
    }

    private void deleteReflection(ModelReflection reflection, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query to find the specific document to delete
        firestore.collection("kidsReflection")
                .whereEqualTo("email", reflection.getEmail())
                .whereEqualTo("controlId", reflection.getControlId())
                .whereEqualTo("timestamp", reflection.getTimestamp())
                .whereEqualTo("reflectionanswer", reflection.getReflectionanswer())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first matching document
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Delete the document
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove from local list
                                    list.remove(position);

                                    // Notify adapter about item removal
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, list.size());

                                    Toast.makeText(context, "Reflection deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to delete reflection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "No matching reflection found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to find reflection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder class
    static class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView emailTextView, timestampTextView, reflectionanswer;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imagenon); // Profile image view
            emailTextView = itemView.findViewById(R.id.namep); // Email text view
            reflectionanswer = itemView.findViewById(R.id.message);
            timestampTextView = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);// Timestamp text view
        }
    }
}
