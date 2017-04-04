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
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.scorpion.sleep.Model.Friends;
import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.util.UserContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationActivity extends AppCompatActivity {
    private RecyclerView recommendationView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> m_recommendationList;
    private List<Friends> recommendationList;
    private Button inviteButton;
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
        setContentView(R.layout.activity_recommendation);
        setTitle("Recommended Friend");
        _context = this;
        final Gson gson = new Gson();
        final UserContext userContext = UserContext.getUserContext(_context.getApplicationContext());
        owner_UID = userContext.getUID();

        // Lookup the recyclerview in activity layout
        recommendationView = (RecyclerView) findViewById(R.id.recommendation_friend_recycler_view);
        httpResp = (TextView) findViewById(R.id.httpResp);

        getRecommendationList(owner_UID, gson);
        // use a linear layout manager

        m_recommendationList = new ArrayList<>();
        recommendationList = new ArrayList<>();
        // specify an adapter (see also next example)
        mAdapter = new RecommendationActivity.RecommendationAdapter(this, recommendationList);
        recommendationView.setAdapter(mAdapter);
        recommendationView.setLayoutManager(new LinearLayoutManager(this));
    }

    public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
        //private List<Friends> recommendationList;
        private Context mcontext;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            // each data item is just a string in this case
            public TextView FriendName;
            public ImageView avatar;

            public ViewHolder(View itemView) {
                super(itemView);

                FriendName = (TextView) itemView.findViewById(R.id.invite_friend_name);
                avatar     = (ImageView) itemView.findViewById(R.id.imageView);
                inviteButton  = (Button) itemView.findViewById(R.id.invite_button);
                inviteButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int itemPosition = getAdapterPosition();
                Friends thisFriend = recommendationList.get(itemPosition);
                String item = thisFriend.getLinks().get(0).getHref();
                int index=item.lastIndexOf('/');
                String uid = item.substring(index+1);
                recommendationList.remove(itemPosition);
                if (v.getId() == inviteButton.getId()) {
                    //Toast.makeText(v.getContext(), "works", Toast.LENGTH_SHORT).show();
                    sendInvitation(owner_UID, uid);
                }
                mAdapter.notifyDataSetChanged();
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public RecommendationAdapter(Context contexts, List<Friends> recommendationList) {
            this.mcontext = contexts;
            //this.recommendationList = new ArrayList<>();
            //this.recommendationList = recommendationList;
        }

        private Context getContext(){
            return mcontext;
        }
        // Create new views (invoked by the layout manager)
        @Override
        public RecommendationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                int viewType) {
            // create a new view
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View contactView = inflater.inflate(R.layout.recommendation_item, parent, false);

            contactView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view) {
                    int itemPosition = recommendationView.getChildLayoutPosition(view);
                    Friends thisFriend = recommendationList.get(itemPosition);
                    String item = thisFriend.getLinks().get(0).getHref();
                    String thisName = thisFriend.getFirstName() + " " + thisFriend.getLastName();
                    Toast.makeText(mcontext, item, Toast.LENGTH_LONG).show();

                    Intent myIntent = new Intent(mcontext, RecommendationProfileActivity.class);
                    myIntent.putExtra("url", item);
                    myIntent.putExtra("name", thisName);
                    startActivity(myIntent);
                }
            });

            // set the view's size, margins, paddings and layout parameters
            RecommendationAdapter.ViewHolder vh = new RecommendationAdapter.ViewHolder(contactView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecommendationAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Friends thisFriend = recommendationList.get(position);
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
            return recommendationList.size();
        }
    }

    //Helper Function
    private void sendInvitation(String owner_uid, String uid){
        String url = UserContext.HW_REMOTE_API_INVITE+"uid="+owner_uid+"&listof="+uid;
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


    private void getRecommendationList(final String uid, final Gson gson) {
        final NetworkManager manager = NetworkManager.inst(_context.getApplicationContext());

        // VERY IMPORTANT: the volley request is Asynchronous, we must put view change inside onResponse function
        String su_url = UserContext.HW_REMOTE_API_RECOM + uid;
        JsonRequest singleU = new JsonRequest<JSONArray>(
                Request.Method.GET,
                su_url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null) {
                            for (int i = 0; i < response.length(); i ++){
                                try {
                                    JSONObject recommended = response.getJSONObject(i);
                                    Friends friends = gson.fromJson(recommended.toString(),Friends.class);
                                    recommendationList.add(friends);
                                } catch (JSONException e) {
                                    Log.d("Error_getRecLst", e.toString());
                                }
                            }
                            mAdapter.notifyDataSetChanged();
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
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONArray resultList = null;
                    if (jsonString.length() > 0){
                        // it's jsonArray rather than single json object
                        resultList = new JSONArray(jsonString);
                    }

                    return Response.success(resultList,
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


}
