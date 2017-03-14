package com.scorpion.sleep;

import android.content.Context;
import android.os.Bundle;
<<<<<<< HEAD
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
=======
import android.util.Log;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.scorpion.sleep.Model.Friends;
import com.scorpion.sleep.util.NetworkManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;
>>>>>>> 8d8faae... friend list complete

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class FriendProfileActivity extends AppCompatActivity {

    private TextView firstname;
    private TextView lastname;
    private TextView email;
    private TextView universityList;
    private TextView graduationYearList;
    private String url;

<<<<<<< HEAD
=======
    private static final String EMULATOR_LOCAL_API = "http://10.0.2.2:8080/friends/" ;
>>>>>>> 8d8faae... friend list complete
    private static final String DEFAULT_UNIVERSITY = "University of Toronto";
    private static final String DEFAULT_GRADUATION_YEAR = "2013";
    private static final String DEBUG = "DEBUG";
    private static final String LOG_TAG = "volley ppl post ";
    private Friends owner;

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        setTitle(getIntent().getStringExtra("name"));
        _context = this;
        final Gson gson = new Gson();

        url = getIntent().getStringExtra("url");

        setUpUI();
        getSingleUser(url,gson);

    }

    private void getSingleUser(String url, final Gson gson) {
        final NetworkManager manager = NetworkManager.inst(_context.getApplicationContext());

        // VERY IMPORTANT: the volley request is Asynchronous, we must put view change inside onResponse function
        JsonObjectRequest singleU = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            handleResponse(response);
                        } else {
                            Log.d("Error", "empty json response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err_str = handleErrorResponse(error);
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
                        owner = gson.fromJson(jsonString,Friends.class);
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
<<<<<<< HEAD
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

    private void handleResponse(JSONObject response) {
        // debug only, can set invisible if needed
        //Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG, "Owner: " + owner.getFirstName() + owner.getLastName() + owner.getEmail());
        // debug only, can set invisible if needed
        firstname.setText(owner.getFirstName());
        lastname.setText(owner.getLastName());
        email.setText(owner.getEmail());
        if (!owner.getUniversity().equals(null))
            universityList.setText(owner.getUniversity());
        graduationYearList.setText(String.valueOf(owner.getGraduationYear()));
    }

=======
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

    private void handleResponse(JSONObject response) {
        // debug only, can set invisible if needed
        //Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG,"Owner: "+owner.getFirstName() + owner.getLastName() + owner.getEmail());
        // debug only, can set invisible if needed
        firstname.setText(owner.getFirstName());
        lastname.setText(owner.getLastName());
        email.setText(owner.getEmail());
        universityList.setText(DEFAULT_UNIVERSITY);
        graduationYearList.setText(DEFAULT_GRADUATION_YEAR);
    }

>>>>>>> 8d8faae... friend list complete
    private void setUpUI() {
        firstname = (TextView) findViewById(R.id.firstNameValue);
        lastname  = (TextView) findViewById(R.id.lastNameValue);
        email  = (TextView) findViewById(R.id.emailValue);
        universityList  = (TextView) findViewById(R.id.universityList);
        graduationYearList  = (TextView) findViewById(R.id.graduationYearList);
    }

}
