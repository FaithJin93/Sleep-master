package com.scorpion.sleep;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {
    private RecyclerView addFriendView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> m_acceptedFriendList;
    private List<String> m_pendingFriendList;
    private List<Friends> pendingFriendList;
    private Button addButton;
    private Button deleteButton;
    private TextView httpResp;

    // For any hardcoded value, use final help its immutability, and name variable in all CAPS
    // to change default UID, go LoginActivity.java LINE74
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
        setTitle("Pending Friend");
        _context = this;
        final Gson gson = new Gson();
        final UserContext userContext = UserContext.getUserContext(_context.getApplicationContext());
        owner_UID = userContext.getUID();

        // Lookup the recyclerview in activity layout
        addFriendView = (RecyclerView) findViewById(R.id.add_friend_recycler_view);
        httpResp = (TextView) findViewById(R.id.httpResp);

        getSingleUser(owner_UID, gson, true);
        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(mLayoutManager);

        pendingFriendList = new ArrayList<>();
        m_pendingFriendList = new ArrayList<>();
        m_acceptedFriendList = new ArrayList<>();
        // specify an adapter (see also next example)
        mAdapter = new AddFriendAdapter(this, pendingFriendList);
        addFriendView.setAdapter(mAdapter);
        addFriendView.setLayoutManager(new LinearLayoutManager(this));

    }

    public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
        private List<Friends> addFriendList;
        private Context mcontext;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            // each data item is just a string in this case
            public TextView FriendName;
            public TextView ButtonText;
            public ImageView avatar;

            public ViewHolder(View itemView) {
                super(itemView);

                FriendName = (TextView) itemView.findViewById(R.id.add_friend_name);
                avatar     = (ImageView) itemView.findViewById(R.id.imageView);
                addButton  = (Button) itemView.findViewById(R.id.add_button);
                addButton.setOnClickListener(this);
                deleteButton  = (Button) itemView.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int itemPosition = getAdapterPosition();
                Friends thisFriend = addFriendList.get(itemPosition);
                String item = thisFriend.get_links().getSelf().getHref();
                int index=item.lastIndexOf('/');
                String uid = item.substring(index+1);
                addFriendList.remove(itemPosition);
                if (v.getId() == addButton.getId()) {
                    //Toast.makeText(v.getContext(), "works", Toast.LENGTH_SHORT).show();
                    changeFriendList(true, uid);
                }
                else if (v.getId() == deleteButton.getId())
                    changeFriendList(false, uid);
                mAdapter.notifyDataSetChanged();
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

            contactView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    int itemPosition = addFriendView.getChildLayoutPosition(view);
                    Friends thisFriend = addFriendList.get(itemPosition);
                    String item = thisFriend.get_links().getSelf().getHref();
                    String thisName = thisFriend.getFirstName() + " " + thisFriend.getLastName();
                    Toast.makeText(mcontext, item, Toast.LENGTH_LONG).show();

                    Intent myIntent = new Intent(mcontext, AddFriendProfileActivity.class);
                    myIntent.putExtra("url", item);
                    myIntent.putExtra("name", thisName);
                    startActivity(myIntent);
                }
            });

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

            ColorGenerator generator = ColorGenerator.MATERIAL;
            // generate color based on name, so it will always have name,color relation
            int color = generator.getColor(thisName);

            String lastnameCapital = thisFriend.getLastName().substring(0, 1).toUpperCase();

            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(lastnameCapital, color);

            holder.FriendName.setText(thisName);
            holder.avatar.setImageDrawable(drawable);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return addFriendList.size();
        }
    }

    private void updateFriendList(String owner_uid, String uid , Boolean add){
        String url = UserContext.HW_REMOTE_API_ACCEPT+"uid="+owner_uid+"&friend_uid="+uid + "&add=" + add;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null ){
                            httpResp.setText("Succ: " + response.toString());
                        }
                        else{
                            Log.d("CRUD","expected empty json response");
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err_str = handleErrorResponse(error);
                        httpResp.setText(err_str);
                    }
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONObject result = null;
                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);
                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                params.put("user-agent", "android");
                Log.d("TEST-PPL", "Params for insert ppl post");
                //printParamsLog(params);
                return params;
            }
        };
        NetworkManager.inst(_context.getApplicationContext()).submitRequest(jsonRequest);
    }

    //Helper Function
    private void getSingleUser(final String uid, final Gson gson, final boolean isOwner) {
        final NetworkManager manager = NetworkManager.inst(_context.getApplicationContext());

        // VERY IMPORTANT: the volley request is Asynchronous, we must put view change inside onResponse function
        String su_url = UserContext.HW_REMOTE_API + uid;
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

    private void changeFriendList(Boolean add, String uid){
        m_pendingFriendList.remove(uid);
        if(add)
            m_acceptedFriendList.add(uid);
//        HashMap<String, String> params = new HashMap<String, String>();
//        String json = new Gson().toJson(m_pendingFriendList);
//        params.put("pendingFriendList", json);
//        json = new Gson().toJson(m_acceptedFriendList);
//        params.put("acceptedFriendList", json);
        updateFriendList(owner_UID, uid, add);

    }

    private void handleResponseIsOwner(Friends friends) {
        // debug only, can set invisible if needed
        final Gson gson = new Gson();
        //Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG, "Owner: " + friends.getFirstName() + friends.getLastName() + friends.getEmail());
        m_acceptedFriendList = friends.getAcceptedFriendList();
        m_pendingFriendList = friends.getPendingFriendList();
        // debug only, can set invisible if needed
        if (m_pendingFriendList != null) {
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
