package com.scorpion.sleep;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


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


    public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

        private String[] friendList;
        private Context mcontext;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView FriendName;

            public ViewHolder(View itemView) {
                super(itemView);

                FriendName = (TextView) itemView.findViewById(R.id.friendName);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public FriendListAdapter(Context contexts, String[] friendList) {
            this.mcontext = contexts;
            this.friendList = friendList;
        }

        private Context getContext(){
            return mcontext;
        }
        // Create new views (invoked by the layout manager)
        @Override
        public FriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
            // create a new view
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View contactView = inflater.inflate(R.layout.friend_list_card, parent, false);

            contactView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    int itemPosition = friendListView.getChildLayoutPosition(view);
                    String item = friendList[itemPosition];
                    Toast.makeText(mcontext, item, Toast.LENGTH_LONG).show();

                    Intent myIntent = new Intent(this, NextActivity.class);
                    myIntent.putExtra("uid", 666);
                    startActivity(myIntent);
                }
            });

            // set the view's size, margins, paddings and layout parameters

            ViewHolder vh = new ViewHolder(contactView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.FriendName.setText(friendList[position]);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return friendList.length;
        }
    }

}
