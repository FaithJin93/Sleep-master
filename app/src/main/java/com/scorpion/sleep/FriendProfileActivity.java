package com.scorpion.sleep;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FriendProfileActivity extends AppCompatActivity {

    private TextView httpResp;

    private String url ;
    private String name;
    private static final String debug = "DEBUG";

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        setTitle(getIntent().getStringExtra("name"));
        _context = this;

        url = getIntent().getStringExtra("url");

    }
}