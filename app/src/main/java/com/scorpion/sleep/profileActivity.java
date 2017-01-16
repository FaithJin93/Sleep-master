package com.scorpion.sleep;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ArrayAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scorpion.sleep.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class profileActivity extends AppCompatActivity {


    private EditText firstname;
    private EditText lastname;
    private Button saveButton;
    private Button postButton;
    private TextView httpResp;

    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        _context = this;
        String university = "University of Toronto";
        String graduationYear = "2013";

        firstname = (EditText) findViewById(R.id.firstNameValue);
        lastname = (EditText) findViewById(R.id.lastNameValue);
        saveButton = (Button) findViewById(R.id.saveButton);
        postButton = (Button) findViewById(R.id.postButton);
        httpResp = (TextView) findViewById(R.id.httpResp);

        Spinner spinnerUniversity = (Spinner) findViewById(R.id.universityList);
        ArrayAdapter<CharSequence> adapterUniversity = ArrayAdapter.createFromResource(this,R.array.universityArray,android.R.layout.simple_spinner_item);
        adapterUniversity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUniversity.setAdapter(adapterUniversity);
        if (!university.equals(null)) {
            int spinnerPosition = adapterUniversity.getPosition(university);
            spinnerUniversity.setSelection(spinnerPosition);
        }

        Spinner spinnerYear = (Spinner) findViewById(R.id.graduationYearList);
        ArrayAdapter<CharSequence> adapterYear = ArrayAdapter.createFromResource(this,R.array.yearArray,android.R.layout.simple_spinner_item);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);
        if (!graduationYear.equals(null)) {
            int spinnerPosition = adapterYear.getPosition(graduationYear);
            spinnerYear.setSelection(spinnerPosition);
        }

        firstname.setText("Eric");
        lastname.setText("Gin");

        //firstname.setVisibility(View.INVISIBLE);

        saveButton.setOnClickListener(new OnClickListener() {

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


        /*
        postButton.setOnClickListener(new OnClickListener() {
            String fn = firstname.getText().toString();
            String ln = lastname.getText().toString();

            @Override
            public void onClick(View view) {
                String fn = firstname.getText().toString();
                String ln = lastname.getText().toString();
                String url = "http://104.131.60.15:8080/people";

                StringRequest jsonRequest = new StringRequest(
                        Request.Method.POST,
                        url,
                        new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(_context, response.toString(), Toast.LENGTH_SHORT).show();
                                httpResp.setText(response.toString());
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                httpResp.setText(error.toString());
                            }
                        }

                ) {
                    @Override
                    protected Map<String,String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String,String>();
                        params.put("firstName",getFirstName());
                        params.put("lastName",getLastName());
                        Log.d("TEST-PPL", "Params for insert ppl post");
                        printParamsLog(params);
                        return params;
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
        });

        */



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
        });



    }

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
