package com.scorpion.sleep;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scorpion.sleep.util.NetworkManager;

import org.json.JSONArray;
import org.w3c.dom.Text;

public class profileActivity extends Activity {


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

        firstname = (EditText) findViewById(R.id.firstNameValue);
        lastname = (EditText) findViewById(R.id.lastNameValue);
        saveButton = (Button) findViewById(R.id.saveButton);
        postButton = (Button) findViewById(R.id.postButton);
        httpResp = (TextView) findViewById(R.id.httpResp);

        firstname.setText("Eric");
        lastname.setText("Jin");

        //firstname.setVisibility(View.INVISIBLE);

        saveButton.setOnClickListener(new OnClickListener() {

            public void onClick(final View view) {
                String fn = firstname.getText().toString();
                String ln = lastname.getText().toString();

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

        saveButton.setOnClickListener(new OnClickListener() {
            String fn = firstname.getText().toString();
            String ln = lastname.getText().toString();

            @Override
            public void onClick(View view) {
                String fn = firstname.getText().toString();
                String ln = lastname.getText().toString();
            }
        });


    }
}
