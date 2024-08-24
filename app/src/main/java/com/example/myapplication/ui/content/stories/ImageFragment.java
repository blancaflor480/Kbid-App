package com.example.myapplication.ui.content.stories;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ImageFragment extends Fragment {

    private OnImageSelectedListener onImageSelectedListener;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // Interface for communicating with the adapter
    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the launcher for the image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (onImageSelectedListener != null) {
                            onImageSelectedListener.onImageSelected(imageUri);
                        }
                    }
                }
        );
    }

    // Method to set the listener from the AdapterUser class
    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.onImageSelectedListener = listener;
    }

    // Method to open the file chooser
    public void openFileChooser() {
        if (isAdded()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageLauncher.launch(intent);  // Launch the intent using the registered launcher
        } else {
            // Handle the case where the fragment is not attached
            // You might want to log an error or notify the user
        }
    }
}
