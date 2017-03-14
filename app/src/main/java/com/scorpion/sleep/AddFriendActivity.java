package com.scorpion.sleep;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.scorpion.sleep.Model.Friends;
import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.util.UserContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    private RecyclerView addFriendView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //private List<Friends> acceptedFriendList;
    private List<Friends> pendingFriendList;

    // For any hardcoded value, use final help its immutability, and name variable in all CAPS
    // to change default UID, go LoginActivity.java LINE74
    private static final String EMULATOR_LOCAL_API = "http://10.0.2.2:8080/friends/" ;
    private static String owner_UID;

    // Log Flags
    private static final String DEBUG = "DEBUG";
    private static final String LOG_TAG = "volley ppl post ";
    //private Friends owner;

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        setTitle("Add Friend");
        _context = this;
        final Gson gson = new Gson();
        final UserContext userContext = UserContext.getUserContext(_context.getApplicationContext());
        owner_UID = userContext.getUID();

        // Lookup the recyclerview in activity layout
        addFriendView = (RecyclerView) findViewById(R.id.add_friend_recycler_view);

        getSingleUser(owner_UID, gson, true);
        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(mLayoutManager);

        pendingFriendList = new ArrayList<>();
        // specify an adapter (see also next example)
        mAdapter = new AddFriendAdapter(this, pendingFriendList);
        addFriendView.setAdapter(mAdapter);
        addFriendView.setLayoutManager(new LinearLayoutManager(this));

    }

    public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
        private List<Friends> addFriendList;
        private Context mcontext;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView FriendName;
            public TextView ButtonText;

            public ViewHolder(View itemView) {
                super(itemView);

                FriendName = (TextView) itemView.findViewById(R.id.add_friend_name);
                ButtonText = (TextView) itemView.findViewById(R.id.add_button);
            }

        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public AddFriendAdapter(Context contexts, List<Friends> friendList) {
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
            Friends thisFriend = addFriendList.get(position);
            String thisName = thisFriend.getFirstName() + " " + thisFriend.getLastName();
            holder.FriendName.setText(thisName);
            holder.ButtonText.setText("Add");
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return addFriendList.size();
        }
    }

    //Helper Function
    private void getSingleUser(final String uid, final Gson gson, final boolean isOwner) {
        final NetworkManager manager = NetworkManager.inst(_context.getApplicationContext());

        // VERY IMPORTANT: the volley request is Asynchronous, we must put view change inside onResponse function
        String su_url = EMULATOR_LOCAL_API + uid;
        JsonObjectRequest singleU = new JsonObjectRequest(
                Request.Method.GET,
                su_url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Friends friends = gson.fromJson(response.toString(),Friends.class);
                            if(uid.equals(owner_UID))
                                handleResponseIsOwner(friends);
                            else
                                handleResponseNotOwner(friends);
                        } else {
                            Log.d("Error", "empty json response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err_str = handleErrorResponse(error);
                        Log.e("ERROR",err_str);
                    }
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONObject result = null;
                    if (jsonString.length() > 0){
                        result = new JSONObject(jsonString);
                    }

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        manager.submitRequest(singleU);
    }

    private String handleErrorResponse(VolleyError error) {
        NetworkResponse networkR = error.networkResponse;
        String defaultError = "Unkown error";
        if (networkR == null){
            if (error.getClass().equals(TimeoutError.class))
                defaultError = "Request Timeout";
            else if (error.getClass().equals(NoConnectionError.class))
                defaultError = "Failed to connect server";
        } else {
            String result = new String(networkR.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");
                String message = response.getString("message");
                Log.e(DEBUG,"status: "+status + " message: "+message);

                switch (networkR.statusCode){
                    case 404:
                        defaultError = "User Not Found";
                        break;
                    case 401:
                        defaultError = "Please Log In Again";
                        break;
                    case 400:
                        defaultError = "Check Your Input";
                        break;
                    case 500:
                        defaultError = "Internal Server Error, Something is Going Wrong";
                        break;
                }
            } catch (JSONException je){
                je.printStackTrace();
            }
        }
        error.printStackTrace();
        Log.d(LOG_TAG, defaultError);
        return defaultError;
    }

    private void handleResponseIsOwner(Friends friends) {
        // debug only, can set invisible if needed
        final Gson gson = new Gson();
        //Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG, "Owner: " + friends.getFirstName() + friends.getLastName() + friends.getEmail());
        // debug only, can set invisible if needed
        if (friends.getPendingFriendList() != null) {
            for (String uid: friends.getPendingFriendList()){
                getSingleUser(uid, gson, false);
            }
        }
        else{
            Log.d(DEBUG,"I am so lonely, i don't have any friends");
        }
    }

    private void handleResponseNotOwner(Friends friends) {
        // debug only, can set invisible if needed
        //Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG, "friends: " + friends.getFirstName() + friends.getLastName() + friends.getEmail());
        // debug only, can set invisible if needed
        pendingFriendList.add(friends);
        // NEED to notify the adapter that data has been changed!
        mAdapter.notifyDataSetChanged();
    }


}
