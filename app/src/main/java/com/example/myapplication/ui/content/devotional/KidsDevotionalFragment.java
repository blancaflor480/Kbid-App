package com.example.myapplication.ui.content.devotional;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.content.stories.ModelStories;
import com.example.myapplication.ui.user.AdapterUser;
import com.example.myapplication.ui.user.ModelUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KidsDevotionalFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private Uri audioUri;
    private StorageReference storageRef;
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterDevotional adapterDevotional;
    private List<ModelDevotional> contentList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private RadioGroup switchMemoryVerse;
    private RadioButton allMemoryVerse;
    private RadioButton viewReflection;
    private ImageView ivImagePreview;

    public KidsDevotionalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devotiona_admin, container, false);

        db = FirebaseFirestore.getInstance(); // Initialize FirebaseFirestore before use

        SearchView searchBar = view.findViewById(R.id.search_bar);
        notFoundTextView = view.findViewById(R.id.not_found_message);
        switchMemoryVerse = view.findViewById(R.id.switch_memoryverse);
        allMemoryVerse = view.findViewById(R.id.Allmemoryverse);
        viewReflection = view.findViewById(R.id.viewreflection);

        // Set initial style for RadioButtons
        setRadioButtonStyle(allMemoryVerse, true);
        setRadioButtonStyle(viewReflection, false);

        // Set up listener for RadioGroup
        switchMemoryVerse.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioButtonStyles(allMemoryVerse, viewReflection);

            if (checkedId == R.id.Allmemoryverse) {
                setRadioButtonStyle(allMemoryVerse, true);
                loadAllmemoryverse(); // Load achievements for All Memory Verse
            } else if (checkedId == R.id.viewreflection) {
                setRadioButtonStyle(viewReflection, true);
                loadviewreflection(); // Load achievements for View Reflection
            }
        });

        // Load initial achievements
        loadAllmemoryverse();

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contentList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
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

    private void setRadioButtonStyle(RadioButton radioButton, boolean isSelected) {
        if (isSelected) {
            radioButton.setTypeface(null, Typeface.BOLD);
            radioButton.setTextColor(getResources().getColor(android.R.color.black));
            radioButton.setBackgroundResource(R.drawable.bg_selected_achievement);
        } else {
            radioButton.setTypeface(null, Typeface.NORMAL);
            radioButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            radioButton.setBackgroundResource(R.drawable.bg_unselected_achievement);
        }
    }

    private void resetRadioButtonStyles(RadioButton... buttons) {
        for (RadioButton button : buttons) {
            setRadioButtonStyle(button, false);
        }
    }

    private void loadAllmemoryverse() {
        CollectionReference devotionalRef = db.collection("devotional");
        devotionalRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    notFoundTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }
                if (value == null || value.isEmpty()) {
                    notFoundTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    notFoundTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    contentList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        ModelDevotional devotional = document.toObject(ModelDevotional.class);
                        contentList.add(devotional);
                    }
                    adapterDevotional = new AdapterDevotional(getActivity(), contentList);
                    recyclerView.setAdapter(adapterDevotional);
                }
            }
        });
    }

    private void loadviewreflection() {
        CollectionReference reflectionRef = db.collection("kidsReflection");
        reflectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    notFoundTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }
                if (value == null || value.isEmpty()) {
                    notFoundTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    notFoundTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    contentList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        ModelDevotional reflection = document.toObject(ModelDevotional.class);
                        contentList.add(reflection);
                    }
                    adapterDevotional = new AdapterDevotional(getActivity(), contentList);
                    recyclerView.setAdapter(adapterDevotional);
                }
            }
        });
    }

    private void filterContent(String query) {
        List<ModelDevotional> filteredList = new ArrayList<>();
        for (ModelDevotional item : contentList) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    item.getVerse().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapterDevotional.filterList(filteredList);
    }

    private void showAddContentDialog() {
        View addView = getLayoutInflater().inflate(R.layout.dialog_add_memoryverse, null);

        EditText etTitle = addView.findViewById(R.id.title);
        EditText etVerse = addView.findViewById(R.id.verse);
        EditText etMemoryverse = addView.findViewById(R.id.memoryverse);
        EditText etDate = addView.findViewById(R.id.datedaily);
        ivImagePreview = addView.findViewById(R.id.thumbnail);
        Button btnUploadImage = addView.findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        etDate.setOnClickListener(v -> showDatePicker(etDate));

        new AlertDialog.Builder(getContext())
                .setView(addView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String verse = etVerse.getText().toString();
                    String memoryVerse = etMemoryverse.getText().toString();
                    String dateStr = etDate.getText().toString();

                    if (title.isEmpty() || verse.isEmpty() || memoryVerse.isEmpty() || dateStr.isEmpty()) {
                        Snackbar.make(getView(), "All fields are required.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // Convert dateStr to a Date object for timestamp
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date timestamp;
                    try {
                        timestamp = sdf.parse(dateStr); // Convert the date string to Date
                    } catch (Exception e) {
                        Snackbar.make(getView(), "Invalid date format.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate the selected date before uploading
                    checkDateAndUpload(timestamp, title, verse, memoryVerse);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDatePicker(EditText etDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(etDate, calendar);
        };

        new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(EditText etDate, Calendar calendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    // Check if a devotional already exists for the given date
    private void checkDateAndUpload(Date timestamp, String title, String verse, String memoryVerse) {
        db.collection("devotional")
                .whereEqualTo("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // A devotional already exists for this date
                        Snackbar.make(getView(), "A devotional already exists for this date.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        // No devotional exists for this date; proceed with upload
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri, title, verse, memoryVerse, timestamp);
                        } else {
                            uploadStoryToFirestore(null, title, verse, memoryVerse, timestamp);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Failed to check date availability.", Snackbar.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivImagePreview.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String title, String verse, String memoryVerse, Date timestamp) {
        StorageReference imageRef = storageRef.child("Content/devotional/images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();
                    uploadStoryToFirestore(imageUrl, title, verse, memoryVerse, timestamp);
                }))
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Image upload failed.", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void uploadStoryToFirestore(String imageUrl, String title, String verse, String memoryVerse, Date timestamp) {
        ModelDevotional devotional = new ModelDevotional(null, title, verse, memoryVerse, imageUrl, timestamp);

        db.collection("devotional").add(devotional)
                .addOnSuccessListener(documentReference -> {
                    devotional.setId(documentReference.getId());
                    db.collection("devotional").document(documentReference.getId()).set(devotional)
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(getView(), "Devotional added.", Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(getView(), "Failed to update devotional with ID.", Snackbar.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Failed to add devotional.", Snackbar.LENGTH_SHORT).show();
                });
    }

}
