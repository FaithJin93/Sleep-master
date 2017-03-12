package com.scorpion.sleep;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.scorpion.sleep.util.ConversionUtils;
import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.util.UserContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends Activity {

    private ScrollView _root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _root = new ScrollView(this);
        setContentView(_root);

        sendInfoRequest();

    }

    private void sendInfoRequest()
    {
        final NetworkManager manager = NetworkManager.inst(getApplicationContext());
        final UserContext userContext = UserContext.getUserContext(getApplicationContext());

        StringRequest jsonRequest = new StringRequest(Request.Method.POST,
                NetworkManager.USERINFO_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(response);
                    }
                },
                manager.getDefaultErrorListener())
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("user-agent", "android");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", (userContext.getUID()));
                Log.d("TEST", "uid="+params.get("uid"));
                return params;
            }
        };

        manager.submitRequest(jsonRequest);
    }

    private void handleResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            boolean success = obj.getBoolean("success");
            if (!success) {
                String error = obj.getString("error");
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            } else {
                String name = obj.getString("name");
                String email = obj.getString("email");
                String invoiceDate = obj.getString("invoiceDate");
                boolean subscription = obj.getBoolean("subscription");
                showData(name, email, invoiceDate, subscription);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error downloading info!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showData(String name, String email, String invoiceDate, boolean subscription) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(getCell(inflater, "Username", name));
        layout.addView(getCell(inflater, "Email", email));
        layout.addView(getCell(inflater, "Last invoice", ConversionUtils.dateToString(invoiceDate)));
        layout.addView(getCell(inflater, "Subscribed?", subscription ? "Yes" : "No"));
        if (subscription) {
            layout.addView(getCell(inflater, "Days since last invoice", Integer.toString(getDaysDelta(invoiceDate, System.currentTimeMillis()))));
        }

        _root.addView(layout);
    }

    private View getCell(LayoutInflater inflater, String title, String data) {
        View v = inflater.inflate(R.layout.user_info_cell, _root, false);
        ((TextView) v.findViewById(R.id.field_name)).setText(title);
        ((TextView) v.findViewById(R.id.field_content)).setText(data);
        return v;
    }

    private int getDaysDelta(String time1, long time2) {
        long initial = ConversionUtils.dateToMillis(time1);
        long delta = time2 - initial;
        return (int) (delta / 1000 / 60 / 60 / 24);
    }
}
