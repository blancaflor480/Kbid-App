package com.example.myapplication.fragment.devotional;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;

public class DevotionalKids extends AppCompatActivity {
    TextView memoryverse,verse;
    AppCompatButton speechttext,submit;
    EditText answerreflection;
    CardView mon,tue,wed,thu,fri,sat,sun;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.kidsdevotional);
    }
}
