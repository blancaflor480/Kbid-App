package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public class FragmentAchievement extends Fragment {

    ImageView underMaintenance;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);

        // Initialize the ImageView for under maintenance
        underMaintenance = view.findViewById(R.id.undermaintainance); // Fixed spelling

        // Use Glide to load the under maintenance GIF
        Glide.with(this)
                .asGif()
                .load(R.raw.undermaintainance) // Load GIF from raw resources
                .into(underMaintenance);

        return view;
    }
}
