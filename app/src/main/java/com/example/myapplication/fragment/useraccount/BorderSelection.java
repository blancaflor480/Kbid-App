package com.example.myapplication.fragment.useraccount;

import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

import de.hdodenhof.circleimageview.CircleImageView;

public class BorderSelection extends AppCompatActivity {

    private int selectedAvatarResourceId = -1;
    private CircleImageView previouslySelectedAvatar = null;
    private AppDatabase db;
    private UserDao userDao;
    private User currentUser;


}
