package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.AdapterViewPager;
import com.example.myapplication.fragment.FragmentAchievement;
import com.example.myapplication.fragment.FragmentHome;
import com.example.myapplication.fragment.FragmentSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ViewPager2 homepage;
    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    BottomNavigationView bottom_nav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        homepage = findViewById(R.id.homepage);
        bottom_nav = findViewById(R.id.bottom_nav);

        fragmentArrayList.add(new FragmentHome());
        fragmentArrayList.add(new FragmentAchievement());
        fragmentArrayList.add(new FragmentSettings());

        AdapterViewPager adapterViewPager = new AdapterViewPager(this, fragmentArrayList);
        homepage.setAdapter(adapterViewPager);

        homepage.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottom_nav.setSelectedItemId(R.id.itHome);
                        break;
                    case 1:
                        bottom_nav.setSelectedItemId(R.id.itAchievement);
                        break;
                    case 2:
                        bottom_nav.setSelectedItemId(R.id.itSettings);
                        break;
                }
                super.onPageSelected(position);
            }
        });

        bottom_nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.itHome){
                    homepage.setCurrentItem(0);
                } else if (itemId == R.id.itAchievement) {
                    homepage.setCurrentItem(1);
                }else if (itemId == R.id.itSettings) {
                    homepage.setCurrentItem(2);
                }
                return true;
            }
        });
    }
}
