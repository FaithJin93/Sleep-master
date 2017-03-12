package com.scorpion.sleep;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.scorpion.sleep.Model.Friends;
import com.scorpion.sleep.util.NetworkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import com.google.gson.Gson;

public class profileActivity extends AppCompatActivity {

    private EditText firstname;
    private EditText lastname;
    private Button saveButton;
    private Button postButton;
    private TextView httpResp;

    // For any hardcoded value, use final help its immutability, and name variable in all CAPS
    private static final String EMULATOR_LOCAL_API = "http://10.0.2.2:8080/friends/" ;
    private static final String UID = "58c44144dd62294b320de5a5";
    private static final String STEVE_UID = "58c216cd160fe397212f4b3b";
    private static final String DEFAULT_UNIVERSITY = "University of Toronto";
    private static final String DEFAULT_GRADUATION_YEAR = "2013";

    // Log Flags
    private static final String DEBUG = "DEBUG";
    private static final String LOG_TAG = "volley ppl post ";
    private String email;
    private Friends owner;

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Personal Profile");
        _context = this;
        final Gson gson = new Gson();

        setUpUI();
        getSingleUser(STEVE_UID,gson);


        //Added By Steve END
        /*
        String url = "http://10.0.2.2:8080/friends";
        Log.d(DEBUG,"url "+url);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null ){
                            raw = response.toString();
                            Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
                            httpResp.setText("Succ: " + response.toString());
                        }
                        else{
                            Log.d("Error","empty json response");
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String LOG_TAG = "volley ppl post ";
                        Log.d(LOG_TAG, "error with: " + error.getMessage());
                        if (error.networkResponse != null)
                            Log.i(LOG_TAG, "status code: " + error.networkResponse.statusCode);
                        httpResp.setText(error.toString());
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


                    //TODO: this is for http://localhost:8080/friends, so it return lists of user, if for single user, need rewrite parsing logic
                    JSONArray friends = result.getJSONObject("_embedded").getJSONArray("friends");
                    int len = friends.length();
                    JSONArray pending = new JSONArray();
                    JSONArray accepted = new JSONArray();

                    System.out.println("Starts");
                    for (int i = 0; i < len; i ++ ){
                        JSONObject pairs = friends.getJSONObject(i);
                        fn = pairs.getString("firstName");
                        ln = pairs.getString("lastName");
                        if (!pairs.get("email").equals(null))
                            email = pairs.getString("email");
                        if (!pairs.get("pendingFriendList").equals(null) ){
                            pending = pairs.getJSONArray("pendingFriendList");
                        }
                        if (!pairs.get("acceptedFriendList").equals(null) ){
                            accepted = pairs.getJSONArray("acceptedFriendList");
                        }
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

        NetworkManager.inst(_context.getApplicationContext()).submitRequest(jsonRequest);
        */


        //saveButton = (Button) findViewById(R.id.saveButton);
        //postButton = (Button) findViewById(R.id.postButton);


        //firstname.setVisibility(View.INVISIBLE);

        /*saveButton.setOnClickListener(new OnClickListener() {

            public void onClick(final View view) {


                String url = "http://104.131.60.15:8080/people";
                StringRequest request = new StringRequest(Request.Method.GET,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(_context, response, Toast.LENGTH_SHORT).show();
                                httpResp.setText(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Volley", error.toString());
                                httpResp.setText(error.toString());
                            }
                        });

                NetworkManager.inst(_context.getApplicationContext()).submitRequest(request);
            }
        });

        postButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                String url = "http://104.131.60.15:8080/people";

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("firstName",getFirstName());
                params.put("lastName",getLastName());
                printParamsLog(params);

                JsonObjectRequest jsonRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        new JSONObject(params),
                        new Response.Listener<JSONObject>(){
                            @Override
                            public void onResponse(JSONObject response) {
                                if (response != null ){
                                    Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
                                    httpResp.setText("Succ: " + response.toString());
                                }
                                 else{
                                    Log.d("Error","empty json response");
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String LOG_TAG = "volley ppl post ";
                                Log.d(LOG_TAG, "error with: " + error.getMessage());
                                if (error.networkResponse != null)
                                    Log.i(LOG_TAG, "status code: " + error.networkResponse.statusCode);
                                httpResp.setText(error.toString());
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
        });*/



    }

    private void getSingleUser(String uid, final Gson gson) {
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
                            handleResponse(response);
                        } else {
                            Log.d("Error", "empty json response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null)
                            Log.d(LOG_TAG, "status code: " + error.networkResponse.statusCode + error.getMessage());
                        httpResp.setText(error.toString());
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

    private void handleResponse(JSONObject response) {
        // debug only, can set invisible if needed
        Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
        Log.d(DEBUG,"Owner: "+owner.getFirstName() + owner.getLastName() + owner.getEmail());
        // debug only, can set invisible if needed
        httpResp.setText("Succ: " + response.toString());
        firstname.setText(owner.getFirstName());
        lastname.setText(owner.getLastName());
    }

    private void setUpUI() {
        firstname = (EditText) findViewById(R.id.firstNameValue);
        lastname  = (EditText) findViewById(R.id.lastNameValue);
        httpResp  = (TextView) findViewById(R.id.httpResp);

        Spinner spinnerUniversity = (Spinner) findViewById(R.id.universityList);
        ArrayAdapter<CharSequence> adapterUniversity = ArrayAdapter.createFromResource(this,R.array.universityArray,android.R.layout.simple_spinner_item);
        adapterUniversity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinnerUniversity != null;
        spinnerUniversity.setAdapter(adapterUniversity);
        if (!DEFAULT_UNIVERSITY.equals(null)) {
            int spinnerPosition = adapterUniversity.getPosition(DEFAULT_UNIVERSITY);
            spinnerUniversity.setSelection(spinnerPosition);
        }

        Spinner spinnerYear = (Spinner) findViewById(R.id.graduationYearList);
        ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this,R.array.yearArray,android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinnerYear != null;
        spinnerYear.setAdapter(adapterYear);
        if (!DEFAULT_GRADUATION_YEAR.equals(null)) {
            int spinnerPosition = adapterYear.getPosition(DEFAULT_GRADUATION_YEAR);
            spinnerYear.setSelection(spinnerPosition);
        }
    }

    // Those 2 functions might be helpful if update values/PATCH
    public String getFirstName() {
        return firstname.getText().toString();
    }

    public String getLastName() {
        return lastname.getText().toString();
    }

    private void printParamsLog(Map<String, String> params) {
        for (String s : params.keySet()) {
            Log.d("TEST-PPL", s + " : " + params.get(s));
        }
    }

}
