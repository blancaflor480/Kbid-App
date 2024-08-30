package com.example.myapplication.ui.content.stories;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.display.DisplayManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterStories extends RecyclerView.Adapter<AdapterStories.MyHolder> {

    private Context context;
    private List<ModelStories> list;
    private FirebaseAuth firebaseAuth;
    private String id;
    private Uri imageUri;
    private StorageReference storageRef;
    private StoriesFragment storiesFragment;


    public AdapterStories(Context context, List<ModelStories> list) {
        this.storiesFragment = storiesFragment; // Keep a reference to the fragment
        this.context = context;
        this.list = list;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.id = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        final ModelStories content = list.get(position);
        String storyId = content.getId(); // Make sure this returns a valid ID
        String imageUrl = content.getImageUrl();
        String title = content.getTitle();
        Date timestamp = content.getTimestamp();

        holder.title.setText(title);
        if (timestamp != null) {
            String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp);
            holder.timestamp.setText(formattedTimestamp);
        } else {
            holder.timestamp.setText("N/A");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.profiletv);
        } else {
            holder.profiletv.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position, storyId); // Pass the storyId here
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position, String storyId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ModelStories content = list.get(position);
                int itemId = item.getItemId();

                if (itemId == R.id.Viewprof) {
                    // showUserProfileDialog(content);
                    return true;
                } else if (itemId == R.id.Editprof) {
                    // Handle edit profile action
                    showEditDialog(content, position); // Call edit dialog
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    // Handle delete profile action
                    deleteStory(content.getId(), position);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void showEditDialog(ModelStories content, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_stories, null);
        builder.setView(dialogView);

        // Fragment management: Attach ImageFragment to the activity
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("IMAGE_FRAGMENT");

        if (imageFragment == null) {
            imageFragment = new ImageFragment();
            fragmentManager.beginTransaction().add(imageFragment, "IMAGE_FRAGMENT").commit();
            fragmentManager.executePendingTransactions(); // Ensure the transaction is complete
        }

        ImageView imagePreview = dialogView.findViewById(R.id.ivImagePreview);
        EditText titleInput = dialogView.findViewById(R.id.etTitle);
        EditText verseInput = dialogView.findViewById(R.id.etVerse);
        EditText descriptionInput = dialogView.findViewById(R.id.etDescription);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);

        // Populate fields with current story data
        titleInput.setText(content.getTitle());
        verseInput.setText(content.getVerse());
        descriptionInput.setText(content.getDescription());

        // Set current image if available
        if (content.getImageUrl() != null) {
            Glide.with(context).load(content.getImageUrl()).into(imagePreview);
        } else {
            imagePreview.setImageResource(R.drawable.image);
        }

        // Set the listener for image selection
        imageFragment.setOnImageSelectedListener(new ImageFragment.OnImageSelectedListener() {
            @Override
            public void onImageSelected(Uri imageUri) {
                updateImageUri(imageUri, imagePreview);
            }
        });

        // Set up image upload button
        ImageFragment finalImageFragment = imageFragment;
        btnUploadImage.setOnClickListener(v -> {
            if (finalImageFragment.isAdded() && !finalImageFragment.isDetached()) {
                finalImageFragment.openFileChooser();
            } else {
                Toast.makeText(context, "Image picker not available. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Update", (dialog, which) -> {
            // Collect updated data
            String updatedTitle = titleInput.getText().toString().trim();
            String updatedVerse = verseInput.getText().toString().trim();
            String updatedDescription = descriptionInput.getText().toString().trim();

            // Validate input
            if (updatedTitle.isEmpty() || updatedVerse.isEmpty() || updatedDescription.isEmpty()) {
                Toast.makeText(context, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateStory(content.getId(), updatedTitle, updatedVerse, updatedDescription, position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateImageUri(Uri imageUri, ImageView imagePreview) {
        this.imageUri = imageUri;
        if (imagePreview != null) {
            imagePreview.setImageURI(imageUri); // Update ImageView with the new image URI
        }
    }



    private void uploadImageToFirebase(Uri imageUri, String storyId, String updatedTitle, String updatedVerse, String updatedDescription, int position) {
        // Create a StorageReference for the image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("stories/" + storyId + ".jpg");

        // Upload the image
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the image URL after the upload is complete
                    storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        // Update Firestore document with the new image URL and other data
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference storyRef = db.collection("stories").document(storyId);

                        storyRef.update("title", updatedTitle, "verse", updatedVerse, "description", updatedDescription, "imageUrl", downloadUrl.toString())
                                .addOnSuccessListener(aVoid -> {
                                    // Update the local list with the new data
                                    list.get(position).setTitle(updatedTitle);
                                    list.get(position).setVerse(updatedVerse);
                                    list.get(position).setRole(updatedDescription); // Ensure you're updating the correct field
                                    list.get(position).setImageUrl(downloadUrl.toString()); // Update the image URL
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Story updated successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update story: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateStory(String storyId, String updatedTitle, String updatedVerse, String updatedDescription, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference storyRef = db.collection("stories").document(storyId);

        // Check if imageUri is available before updating the document
        if (imageUri != null) {
            uploadImageToFirebase(imageUri, storyId, updatedTitle, updatedVerse, updatedDescription, position);
        } else {
            // Only update text fields if no new image is selected
            storyRef.update("title", updatedTitle, "verse", updatedVerse, "description", updatedDescription)
                    .addOnSuccessListener(aVoid -> {
                        list.get(position).setTitle(updatedTitle);
                        list.get(position).setVerse(updatedVerse);
                        list.get(position).setRole(updatedDescription); // Ensure you're updating the correct field
                        notifyItemChanged(position);
                        Toast.makeText(context, "Story updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                        Toast.makeText(context, "Failed to update story", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    private void deleteStory(String storyId, int position) {
        // Check if storyId is null
        if (storyId == null) {
            Toast.makeText(context, "Error: Story ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit the method if storyId is null
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        new AlertDialog.Builder(context)
                .setTitle("Delete Story")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("stories").document(storyId).delete()
                            .addOnSuccessListener(aVoid -> {
                                list.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Story deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error deleting story: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }





    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profiletv;
        TextView title, timestamp;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagenon);
            title = itemView.findViewById(R.id.titlep);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
