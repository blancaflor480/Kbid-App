package com.example.myapplication.ui.content.devotional;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import java.text.ParseException;
import java.util.Calendar;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterDevotional extends RecyclerView.Adapter<AdapterDevotional.MyHolder> {
    private final Context context;
    private final List<ModelDevotional> list;
    private final FirebaseAuth firebaseAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageRef;
    private ImageView ivImagePreview;
    private Uri imageUri;
    public AdapterDevotional(Context context, List<ModelDevotional> list) {
        this.context = context;
        this.list = list;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AdapterDevotional.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_devotional, parent, false);
        return new AdapterDevotional.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDevotional.MyHolder holder, int position) {
        ModelDevotional content = list.get(position);
        String imageUrl = content.getImageUrl();
        String title = content.getTitle();
        Date timestamp = content.getTimestamp();
        String id = content.getId();

        holder.title.setText(title);
        holder.timestamp.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp) : "N/A");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position, content.getId());
            }
        });
    }

    private void showPopupMenu(View view, int position, String id) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_devotional, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ModelDevotional content = list.get(position);
                int itemId = item.getItemId();

                if (itemId == R.id.Viewprof) {
                    // showViewDialog(content);
                    return true;
                } else if (itemId == R.id.Feedback) {
                    // showEditDialog(content);
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Reflection")
                            .setMessage("Are you sure you want to delete this reflection?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // Perform deletion
                                //           deleteReflection(content, position);
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

    private void showEditDialog(ModelDevotional content) {
        View editView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_memoryverse, null);

        EditText etTitle = editView.findViewById(R.id.title);
        EditText etVerse = editView.findViewById(R.id.verse);
        EditText etMemoryverse = editView.findViewById(R.id.memoryverse);
        EditText etDate = editView.findViewById(R.id.datedaily);
        ImageView ivImagePreview = editView.findViewById(R.id.thumbnail);
        Button btnUploadImage = editView.findViewById(R.id.btnUploadImage);

        // Populate existing data
        etTitle.setText(content.getTitle());
        etVerse.setText(content.getVerse());
        etMemoryverse.setText(content.getMemoryverse());

        // Format and set existing date
        if (content.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            etDate.setText(sdf.format(content.getTimestamp()));
        }

        // Load existing image if available
        if (content.getImageUrl() != null && !content.getImageUrl().isEmpty()) {
            Glide.with(context).load(content.getImageUrl()).into(ivImagePreview);
        }

        // Date picker setup
        etDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(etDate, calendar);
            };

            new DatePickerDialog(context, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Image upload setup
        Uri[] imageUri = new Uri[1];
        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // This should be called from the Activity or Fragment
            ((Activity)context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        new AlertDialog.Builder(context)
                .setView(editView)
                .setTitle("Edit Devotional")
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String verse = etVerse.getText().toString().trim();
                    String memoryVerse = etMemoryverse.getText().toString().trim();
                    String dateStr = etDate.getText().toString().trim();

                    // Validate inputs
                    if (title.isEmpty() || verse.isEmpty() || memoryVerse.isEmpty() || dateStr.isEmpty()) {
                        Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Parse date
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date timestamp;
                    try {
                        timestamp = sdf.parse(dateStr);
                    } catch (ParseException e) {
                        Toast.makeText(context, "Invalid date format.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update process
                    updateDevotional(content, title, verse, memoryVerse, timestamp, imageUri[0]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    public void handleImagePickResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivImagePreview != null) {
                ivImagePreview.setImageURI(imageUri);
            }
        }
    }


    private void updateDevotional(ModelDevotional content, String title, String verse,
                                  String memoryVerse, Date timestamp, Uri imageUri) {
        // If a new image is selected
        if (imageUri != null) {
            uploadImageToFirebaseAndUpdate(content, imageUri, title, verse, memoryVerse, timestamp);
        } else {
            // Update without changing the image
            updateDevotionalInFirestore(content, title, verse, memoryVerse, timestamp, content.getImageUrl());
        }
    }

    private void uploadImageToFirebaseAndUpdate(ModelDevotional content, Uri imageUri,
                                                String title, String verse,
                                                String memoryVerse, Date timestamp) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("Content/devotional/images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            updateDevotionalInFirestore(content, title, verse, memoryVerse, timestamp, imageUrl);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Image upload failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDevotionalInFirestore(ModelDevotional content, String title,
                                             String verse, String memoryVerse,
                                             Date timestamp, String imageUrl) {
        // Update the content object
        content.setTitle(title);
        content.setVerse(verse);
        content.setMemoryverse(memoryVerse);
        content.setTimestamp(timestamp);
        content.setImageUrl(imageUrl);

        // Update in Firestore
        FirebaseFirestore.getInstance().collection("devotional")
                .document(content.getId())
                .set(content)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Devotional updated successfully.", Toast.LENGTH_SHORT).show();
                    // Notify adapter of changes
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update devotional.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLabel(EditText etDate, Calendar calendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }






    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(List<ModelDevotional> filteredList) {
        list.clear();
        list.addAll(filteredList);
        notifyDataSetChanged();
    }


    static class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView title, timestamp;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imagenon);
            title = itemView.findViewById(R.id.titlep);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
