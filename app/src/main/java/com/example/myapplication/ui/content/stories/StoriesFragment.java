package com.example.myapplication.ui.content.stories;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoriesFragment extends Fragment {
    private static final int PICK_AUDIO_REQUEST = 2;
    private Uri audioUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterStories adapterContent;
    private List<ModelStories> contentList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ImageView ivImagePreview;

    public StoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_stories, container, false);

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
                    case 0: // All
                        getAllStories();
                        break;
                    case 1: // New
                        getNewStories();
                        break;
                    case 2: // Old
                        getOldStories();
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

    private void getAllStories() {
        loadContentFromCollection("stories", null, null);
    }

    private void getNewStories() {
        // Get the first day of the current month
        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        // Get the last day of the current month
        Calendar lastDayOfMonth = Calendar.getInstance();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("stories", firstDayOfMonth.getTime(), lastDayOfMonth.getTime());
    }

    private void getOldStories() {
        // Get the first day of the previous month
        Calendar firstDayOfLastMonth = Calendar.getInstance();
        firstDayOfLastMonth.add(Calendar.MONTH, -1);
        firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

        // Get the last day of the previous month
        Calendar lastDayOfLastMonth = Calendar.getInstance();
        lastDayOfLastMonth.add(Calendar.MONTH, -1);
        lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("stories", firstDayOfLastMonth.getTime(), lastDayOfLastMonth.getTime());
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
                ModelStories content = document.toObject(ModelStories.class);
                contentList.add(content);
            }
            adapterContent = new AdapterStories(getActivity(), contentList);
            recyclerView.setAdapter(adapterContent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        notFoundTextView.setVisibility(View.GONE);
        adapterContent = new AdapterStories(getContext(), contentList);
        recyclerView.setAdapter(adapterContent);
    }

    private void filterContent(String query) {
        List<ModelStories> filteredList = new ArrayList<>();
        for (ModelStories content : contentList) {
            String title = content.getTitle() != null ? content.getTitle().toLowerCase() : "";
            if (title.contains(query.toLowerCase())) {
                filteredList.add(content);
            }
        }

        if (filteredList.isEmpty()) {
            notFoundTextView.setVisibility(View.VISIBLE);
        } else {
            notFoundTextView.setVisibility(View.GONE);
        }

        adapterContent = new AdapterStories(getContext(), filteredList);
        recyclerView.setAdapter(adapterContent);
    }

    private void showAddContentDialog() {
        View addView = getLayoutInflater().inflate(R.layout.dialog_add_stories, null);

        // Handle story-specific input fields
        EditText etTitle = addView.findViewById(R.id.etTitle);
        EditText etVerse = addView.findViewById(R.id.etVerse);
        EditText etDescription = addView.findViewById(R.id.etDescription);
        ivImagePreview = addView.findViewById(R.id.ivImagePreview);
        EditText etDate = addView.findViewById(R.id.etDate);
        Spinner spinnerUnlocked = addView.findViewById(R.id.spinnerUnlock);
        Button btnUploadImage = addView.findViewById(R.id.btnUploadImage);
        Button btnUploadAudio = addView.findViewById(R.id.btnUploadAudio);

        // Set up the gender spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.option_status, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnlocked.setAdapter(statusAdapter);

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etDate);
            }
        });

        btnUploadAudio.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_AUDIO_REQUEST);
        });

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Create and show the dialog
        // Create and show the dialog
        new AlertDialog.Builder(getContext())
                .setView(addView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String verse = etVerse.getText().toString();
                    String description = etDescription.getText().toString();
                    String isCompeleted = spinnerUnlocked.getSelectedItem().toString();
                    String selectedDateString = etDate.getText().toString(); // Get the date as a string

                    if (imageUri != null && audioUri != null) {
                        // Upload image first
                        uploadImageToFirebase(imageUri, title, verse, description, isCompeleted, selectedDateString);
                    } else if (audioUri != null) {
                        // Upload only audio
                        uploadAudioToFirebase(audioUri, title, verse, description, isCompeleted, null, selectedDateString);
                    } else if (imageUri != null) {
                        // Upload only image
                        uploadImageToFirebase(imageUri, title, verse, description, isCompeleted, selectedDateString);
                    } else {
                        uploadStoryToFirestore(null, title, verse, description, isCompeleted, null, selectedDateString);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void showDatePicker(EditText etDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(etDate, calendar);
            }
        };

        new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(EditText etDate, Calendar calendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivImagePreview.setImageURI(imageUri);
        } else if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String title, String verse, String description, String isCompleted, String selectedDateString) {
        StorageReference imageRef = storageRef.child("Content/stories/images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();

                    if (audioUri != null) {
                        // If there is an audioUri, upload audio after image
                        uploadAudioToFirebase(audioUri, title, verse, description, isCompleted, imageUrl, selectedDateString);
                    } else {
                        // If no audioUri, just upload the story with the image URL
                        uploadStoryToFirestore(imageUrl, title, verse, description, isCompleted, null, selectedDateString);
                    }
                }))
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Image upload failed.", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void uploadAudioToFirebase(Uri audioUri, String title, String verse, String description, String isCompleted, String imageUrl, String selectedDateString) {
        StorageReference audioRef = storageRef.child("Content/stories/audio/" + System.currentTimeMillis() + ".mp3");

        audioRef.putFile(audioUri)
                .addOnSuccessListener(taskSnapshot -> audioRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String audioUrl = downloadUri.toString();
                    uploadStoryToFirestore(imageUrl, title, verse, description, isCompleted, audioUrl, selectedDateString);
                }))
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Audio upload failed.", Snackbar.LENGTH_SHORT).show();
                });
    }

    // Method to upload story to Firestore
    private void uploadStoryToFirestore(String imageUrl, String title, String verse, String description, String isCompleted, String audioUrl, String selectedDateString) {
        // Parse the date from the input
        Date selectedDate = null;
        try {
            String myFormat = "MM/dd/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            selectedDate = sdf.parse(selectedDateString);
        } catch (ParseException e) {
            // If parsing fails, use current date as fallback
            selectedDate = new Date();
            Snackbar.make(getView(), "Using current date due to parsing error.", Snackbar.LENGTH_SHORT).show();
        }

        // Fetch the current highest count from Firestore
        Date finalSelectedDate = selectedDate;
        db.collection("stories")
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextCount = 1; // Default starting count

                    // If there are stories, get the highest count and increment it
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot highestCountDoc = queryDocumentSnapshots.getDocuments().get(0);
                        nextCount = highestCountDoc.getLong("count").intValue() + 1;
                    }

                    // Create the story model with the selected date
                    ModelStories story = new ModelStories(title, verse, description, imageUrl, null, isCompleted, finalSelectedDate, audioUrl);
                    story.setCount(nextCount);

                    // Add the story to Firestore
                    db.collection("stories").add(story)
                            .addOnSuccessListener(documentReference -> {
                                // Set the document ID in the story model
                                story.setId(documentReference.getId());

                                // Update the Firestore document with the model containing the ID
                                db.collection("stories").document(documentReference.getId()).set(story)
                                        .addOnSuccessListener(aVoid -> {
                                            showUploadSuccessDialog();
                                            Snackbar.make(getView(), "Story added.", Snackbar.LENGTH_SHORT).show();
                                            getAllStories(); // Refresh the list of stories
                                        })
                                        .addOnFailureListener(e -> {
                                            Snackbar.make(getView(), "Failed to update story with ID.", Snackbar.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(getView(), "Failed to add story.", Snackbar.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Failed to fetch current count.", Snackbar.LENGTH_SHORT).show();
                });
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
}
