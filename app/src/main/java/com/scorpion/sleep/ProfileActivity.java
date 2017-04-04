package com.scorpion.sleep;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText firstname;
    private EditText lastname;
    private EditText email;
    private EditText major;
    private TextView httpResp;
    private Button saveButton;
    private Spinner spinnerUniversity;
    ArrayAdapter<CharSequence> adapterUniversity;
    private Spinner spinnerYear;
    ArrayAdapter<CharSequence> adapterYear;

    // For any hardcoded value, use final help its immutability, and name variable in all CAPS
    private static final String DEFAULT_UNIVERSITY = "University of Toronto";
    private static final String DEFAULT_GRADUATION_YEAR = "2013";

    // Log Flags
    private static final String DEBUG = "DEBUG";
    private static final String LOG_TAG = "volley ppl post ";
    private Friends owner;

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Personal Profile");
        _context = this;
        final Gson gson = new Gson();
        final UserContext userContext = UserContext.getUserContext(_context.getApplicationContext());

        setUpUI();
        getSingleUser(userContext.getUID(),gson);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("firstName", getFirstName());
                params.put("lastName", getLastName());
                params.put("email", getEmail());
                params.put("major",getMajor());
                params.put("university", spinnerUniversity.getSelectedItem().toString());
                params.put("graduationYear", spinnerYear.getSelectedItem().toString());

                updateSingleUser(userContext.getUID(), params);

            }
        });

    }

    private void updateSingleUser(String uid, final HashMap params ){
                JsonObjectRequest jsonRequest = new JsonObjectRequest(
                        Request.Method.PATCH,
                        UserContext.HW_REMOTE_API+uid,
                        new JSONObject(params),
                        new Response.Listener<JSONObject>(){
                            @Override
                            public void onResponse(JSONObject response) {
                                httpResp.setText("Update Successful");
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String err_str = handleErrorResponse(error);
                                Log.d("Update", "onErrorResponse: " + err_str);
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
                        printParamsLog(params);
                        return params;
                    }
                };
                NetworkManager.inst(_context.getApplicationContext()).submitRequest(jsonRequest);
    }

    private void getSingleUser(String uid, final Gson gson) {
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
        if (owner!=null && owner.getUniversity()!=null && !owner.getUniversity().equals(null)) {
            int spinnerPosition = adapterUniversity.getPosition(owner.getUniversity());
            spinnerUniversity.setSelection(spinnerPosition);
        }
        if (owner!=null && owner.getMajor()!=null && !owner.getMajor().equals(null)) {
            major.setText(owner.getMajor());
        }
        if (owner!=null && owner.getGraduationYear().equals(null)) {
            int spinnerPosition = adapterYear.getPosition(owner.getGraduationYear());
            spinnerYear.setSelection(spinnerPosition);
        }

    }

    private void setUpUI() {
        firstname = (EditText) findViewById(R.id.firstNameValue);
        lastname  = (EditText) findViewById(R.id.lastNameValue);
        email  = (EditText) findViewById(R.id.emailValue);
        major  = (EditText) findViewById(R.id.majorValue);
        httpResp = (TextView) findViewById(R.id.httpResp);
        saveButton = (Button) findViewById(R.id.saveButton);

        spinnerUniversity = (Spinner) findViewById(R.id.universityList);
        adapterUniversity = ArrayAdapter.createFromResource(this,R.array.universityArray,android.R.layout.simple_spinner_item);
        adapterUniversity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinnerUniversity != null;
        spinnerUniversity.setAdapter(adapterUniversity);

        spinnerYear = (Spinner) findViewById(R.id.graduationYearList);
        adapterYear = ArrayAdapter.createFromResource(this,R.array.yearArray,android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinnerYear != null;
        spinnerYear.setAdapter(adapterYear);
    }

    // Those 2 functions might be helpful if update values/PATCH
    public String getFirstName() {
        return firstname.getText().toString().trim();
    }

    public String getLastName() {
        return lastname.getText().toString().trim();
    }

    public String getEmail() {
        return email.getText().toString().trim();
    }

    public String getMajor(){
        return major.getText().toString().trim();
    }

    private void printParamsLog(Map<String, String> params) {
        for (String s : params.keySet()) {
            Log.d("TEST-PPL", s + " : " + params.get(s));
        }
    }

}