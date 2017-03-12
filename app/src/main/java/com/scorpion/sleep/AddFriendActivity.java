package com.scorpion.sleep;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.scorpion.sleep.util.AddFriendAdapter;

public class AddFriendActivity extends AppCompatActivity {
    private RecyclerView addFriendView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] addFriendList = {"Eric Jin", "Steve Zhang", "Yufan Jin", "April Jing"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        setTitle("Add Friend");

        // Lookup the recyclerview in activity layout
        addFriendView = (RecyclerView) findViewById(R.id.add_friend_recycler_view);

        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new AddFriendAdapter(this, addFriendList);
        addFriendView.setAdapter(mAdapter);
        addFriendView.setLayoutManager(new LinearLayoutManager(this));

    }
}
