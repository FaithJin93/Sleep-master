package com.scorpion.sleep;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.scorpion.sleep.util.ConversionUtils;
import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.util.UserContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends Activity {

    private TextView _numberRecordsTextView;
    private Spinner _spinner;
    private ScrollView _root;
    private HistoryAdapter _spinnerAdapter;
    private List<HistoryItem> _appointments;
    private LayoutInflater _inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);
        _spinner = (Spinner) findViewById(R.id.spinner_appointment);
        _root = (ScrollView) findViewById(R.id.scrollview_appointment);
        _numberRecordsTextView = (TextView) findViewById(R.id.number_appointments_textview);
        _appointments = new ArrayList<HistoryItem>();
        _spinnerAdapter = new HistoryAdapter(this, _appointments);
        _spinner.setAdapter(_spinnerAdapter);
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                displayAppointmentView(_appointments.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        _inflater = LayoutInflater.from(this);
        downloadUserHistory();
    }

    private void downloadUserHistory()
    {
        NetworkManager manager = NetworkManager.inst(this);
        final UserContext userContext = UserContext.getUserContext(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, NetworkManager.MEDHISTORY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseResponse(response);
            }
        }, manager.getDefaultErrorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", userContext.getUID());
                return params;
            }
        };

        manager.submitRequest(request);
    }

    private void parseResponse(String response)
    {
        _appointments.clear();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("success");
            if (!success) {
                return;
            }

            JSONArray outArray = jsonResponse.getJSONArray("out");

            for (int i = 0; i < outArray.length(); i++) {
                JSONObject outResponse = outArray.getJSONObject(i);

                int historyId = outResponse.getInt("history_id");
                int staffId = outResponse.getInt("staff_id");
                int customerId = outResponse.getInt("customer_id");
                String staffName = outResponse.getString("staff_name");
                String customerName = outResponse.getString("customer_name");
                String visitDate = outResponse.getString("visit_date");
                String symptom = outResponse.getString("symptom");
                String resolution = outResponse.getString("resolution");
                String notes = outResponse.getString("additional_note");
                _appointments.add(new HistoryItem(historyId, staffId, customerId, staffName, customerName, visitDate, symptom, resolution, notes));
            }
            _spinnerAdapter.notifyDataSetChanged();
            _spinner.setSelection(_spinnerAdapter.getCount() - 1);
            updateNumberRecords(_spinnerAdapter.getCount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateNumberRecords(int number) {
        String response;
        if (number == 1) {
            response = "You have 1 previous appointment record!";
        } else {
            response = "You have " + number + " previous appointment records!";
        }
        _numberRecordsTextView.setText(response);
        _numberRecordsTextView.setVisibility(View.VISIBLE);
    }

    private void displayAppointmentView(HistoryItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(getCell("Name", item.getCustomerName()));
        layout.addView(getCell("Doctor's Name", item.getStaffName()));
        layout.addView(getCell("Visit Date", ConversionUtils.dateToString(item.getVisitDate())));
        layout.addView(getCell("Symptom", item.getSymptom()));
        layout.addView(getCell("Resolution", item.getResolution()));
        layout.addView(getCell("Notes", item.getNotes()));

        _root.removeAllViews();
        _root.addView(layout);
    }

    private View getCell(String title, String data) {
        View v = _inflater.inflate(R.layout.user_info_cell, _root, false);
        ((TextView) v.findViewById(R.id.field_name)).setText(title);
        ((TextView) v.findViewById(R.id.field_content)).setText(data);
        return v;
    }
}
