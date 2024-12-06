package com.example.myapplication.ui.content.song;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.content.stories.AdapterStories;
import com.example.myapplication.ui.content.stories.ModelStories;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SongFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private Uri imageUri, videoUri;
    private StorageReference storageRef;
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterSong adapterContent;
    private List<ModelSong> contentList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ImageView ivImagePreview;

    public SongFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_admin, container, false);

        Spinner spinner = view.findViewById(R.id.user_dropdown);
        SearchView searchBar = view.findViewById(R.id.search_bar);
        notFoundTextView = view.findViewById(R.id.not_found_message);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.option_stories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        getAllVideo();
                        break;
                    case 1:
                        getNewVideo();
                        break;
                    case 2:
                        getOldVideo();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contentList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddContentDialog());

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterContent(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContent(newText);
                return false;
            }
        });

        return view;
    }

    private void getAllVideo() {
        loadContentFromCollection("video", null, null);
    }

    private void getNewVideo() {
        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar lastDayOfMonth = Calendar.getInstance();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("video", firstDayOfMonth.getTime(), lastDayOfMonth.getTime());
    }

    private void getOldVideo() {
        Calendar firstDayOfLastMonth = Calendar.getInstance();
        firstDayOfLastMonth.add(Calendar.MONTH, -1);
        firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar lastDayOfLastMonth = Calendar.getInstance();
        lastDayOfLastMonth.add(Calendar.MONTH, -1);
        lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("video", firstDayOfLastMonth.getTime(), lastDayOfLastMonth.getTime());
    }

    private void loadContentFromCollection(String collectionName, Date startDate, Date endDate) {
        CollectionReference collectionRef = db.collection(collectionName);
        Query query = collectionRef;

        if (startDate != null && endDate != null) {
            query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate);
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) return;
            contentList.clear();
            for (QueryDocumentSnapshot document : value) {
                ModelSong content = document.toObject(ModelSong.class);
                contentList.add(content);
            }
            adapterContent = new AdapterSong(getActivity(), contentList);
            recyclerView.setAdapter(adapterContent);
        });
    }

    private void showAddContentDialog() {
        View addView = getLayoutInflater().inflate(R.layout.dialog_add_song, null);

        EditText etTitle = addView.findViewById(R.id.etTitle);
        EditText etDescription = addView.findViewById(R.id.etDescription);
        ivImagePreview = addView.findViewById(R.id.ivImagePreview);
        Button btnUploadImage = addView.findViewById(R.id.btnUploadImage);
        Button btnUploadVideo = addView.findViewById(R.id.btnUploadVideo);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnUploadVideo.setOnClickListener(v -> {
            // Update the intent to pick video files instead of audio files
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        new AlertDialog.Builder(getContext())
                .setView(addView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String description = etDescription.getText().toString();

                    if (videoUri != null && imageUri != null) {
                        uploadContentToFirebase(imageUri, videoUri, title, description);
                    } else {
                        Snackbar.make(getView(), "Please upload both image and video.", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivImagePreview.setImageURI(imageUri);
        } else if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();
            Snackbar.make(getView(), "Video file selected.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void uploadContentToFirebase(Uri imageUri, Uri videoUri, String title, String description) {
        StorageReference imageRef = storageRef.child("Content/video/images/" + System.currentTimeMillis() + ".jpg");
        StorageReference videoRef = storageRef.child("Content/video/videos/" + System.currentTimeMillis() + ".mp4");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(imageDownloadUri ->
                        videoRef.putFile(videoUri)
                                .addOnSuccessListener(taskSnapshot1 -> videoRef.getDownloadUrl().addOnSuccessListener(videoDownloadUri ->
                                        saveVideoToFirestore(imageDownloadUri.toString(), videoDownloadUri.toString(), title, description))))
                )
                .addOnFailureListener(e -> Snackbar.make(getView(), "File upload failed.", Snackbar.LENGTH_SHORT).show());
    }

    private void saveVideoToFirestore(String imageUrl, String videoUrl, String title, String description) {
        Date timestamp = new Date();
        ModelSong video = new ModelSong(title, "Video", videoUrl, description, firebaseAuth.getCurrentUser().getEmail(), imageUrl, null, timestamp);

        db.collection("video").add(video)
                .addOnSuccessListener(documentReference -> {
                    video.setId(documentReference.getId());
                    db.collection("video").document(documentReference.getId()).set(video)
                            .addOnSuccessListener(aVoid -> {
                                showUploadSuccessDialog();
                                Snackbar.make(getView(), "Video added.", Snackbar.LENGTH_SHORT).show();
                                getAllVideo();
                            });
                })
                .addOnFailureListener(e -> Snackbar.make(getView(), "Failed to add video.", Snackbar.LENGTH_SHORT).show());
    }

    private void showUploadSuccessDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.uploadedsucessfully, null);

        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        Button btnOkay = dialogView.findViewById(R.id.play);
        btnOkay.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void filterContent(String query) {
        List<ModelSong> filteredList = new ArrayList<>();
        for (ModelSong song : contentList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }
        adapterContent = new AdapterSong(getActivity(), filteredList);
        recyclerView.setAdapter(adapterContent);
    }
}
