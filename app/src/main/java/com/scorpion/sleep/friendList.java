package com.scorpion.sleep;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.scorpion.sleep.util.FriendListAdapter;

public class friendList extends AppCompatActivity {
    private RecyclerView friendListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] friendList = {"Eric Jin", "Steve Zhang", "Yufan Jin","April Jing"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        setTitle("Friend List");

        // Lookup the recyclerview in activity layout
        friendListView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new FriendListAdapter(this,friendList);
        friendListView.setAdapter(mAdapter);
        friendListView.setLayoutManager(new LinearLayoutManager(this));

    }
}