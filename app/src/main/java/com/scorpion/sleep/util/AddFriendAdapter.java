package com.scorpion.sleep.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.scorpion.sleep.R;

/**
 * Created by apple on 2017/1/15.
 */

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
    private String[] addFriendList;
    private Context mcontext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView FriendName;

        public ViewHolder(View itemView) {
            super(itemView);

            FriendName = (TextView) itemView.findViewById(R.id.add_friend_name);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AddFriendAdapter(Context contexts, String[] friendList) {
        this.mcontext = contexts;
        this.addFriendList = friendList;
    }

    private Context getContext(){
        return mcontext;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public AddFriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.add_friend_item, parent, false);

        // set the view's size, margins, paddings and layout parameters

        AddFriendAdapter.ViewHolder vh = new AddFriendAdapter.ViewHolder(contactView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AddFriendAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.FriendName.setText(addFriendList[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return addFriendList.length;
    }
}
