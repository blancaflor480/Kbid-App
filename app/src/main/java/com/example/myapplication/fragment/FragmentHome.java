package com.example.myapplication.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.fragment.biblegames.GamesFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;

public class FragmentHome extends Fragment {
    TextView clickStories;
    TextView clickGames;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the TextView
        clickStories = view.findViewById(R.id.clickStories);


        // Set the click listener for clickStories
        clickStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBibleActivity();
            }
        });

        clickGames = view.findViewById(R.id.clickGames);
        // Set the click listener for clickStories
        clickGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBiblegamesActivity();
            }
        });

        return view;
    }

    private void navigateToBibleActivity() {
        Intent intent = new Intent(getActivity(), BibleFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to BibleActivity");
    }

    private void navigateToBiblegamesActivity() {
        Intent intent = new Intent(getActivity(), GamesFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to Biblegames");
    }
}
