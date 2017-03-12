package com.scorpion.sleep.Booking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.R;
import com.scorpion.sleep.util.UserContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivity extends Activity implements Response.Listener<JSONArray> {

    private RecyclerView _calendarView;
    private List<Day> _data;
    private BookingRecyclerAdapter _calendarAdapter;
    private Context _context;

    interface BookingCallback {
        void onTimeClicked(String date, int timeId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        _context = this;
        _data = new ArrayList<Day>();

        _calendarView = (RecyclerView) findViewById(R.id.booking_recycler_view);
        _calendarAdapter = new BookingRecyclerAdapter(_data, handleTimeClick);
        _calendarView.setLayoutManager(new GridLayoutManager(this, 7));
        _calendarView.setAdapter(_calendarAdapter);

        requestBookingData();
    }

    private void requestBookingData() {
        NetworkManager manager = NetworkManager.inst(this.getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(NetworkManager.CALENDAR_URL, this, manager.getDefaultErrorListener());
        manager.submitRequest(request);
    }

    private BookingCallback handleTimeClick = new BookingCallback() {
        @Override
        public void onTimeClicked(final String date, final int timeId) {

            NetworkManager manager = NetworkManager.inst(_context.getApplicationContext());
            Log.d("CalendarResponse", "Sending Request: " + date + ", " + timeId);

            StringRequest request = new StringRequest(Request.Method.POST, NetworkManager.CALENDAR_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("CalendarResponse", response);
                    try {
                        JSONObject object = new JSONObject(response);
                        boolean success = object.getBoolean("allowed");
                        if (success) {
                            showSuccessDialog();
                        } else {
                            Toast.makeText(_context, "Could not complete request!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(_context, "Could not complete request!", Toast.LENGTH_SHORT).show();
                    }
                }
            }, manager.getDefaultErrorListener()) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("uid", UserContext.getUserContext(_context.getApplicationContext()).getUID());
                    params.put("date", date);
                    params.put("timeslot_id", Integer.toString(timeId));
                    return params;
                }
            };
            manager.submitRequest(request);
        }
    };

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Appointment confirmation with more details will be sent to your email address.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setIcon(android.R.drawable.ic_menu_info_details)
                .show();
    }

    @Override
    public void onResponse(JSONArray response) {
        _data.clear();
        try {
            for (int i = 0; i < response.length(); i++) {
                List<Timeslot> timeslotList = new ArrayList<Timeslot>();

                JSONObject dayObject = response.getJSONObject(i);
                String date = dayObject.getString("date");
                JSONArray timeslots = dayObject.getJSONArray("timeslots");
                for (int j = 0; j < timeslots.length(); j++) {
                    JSONObject slot = timeslots.getJSONObject(j);
                    int id = slot.getInt("id");
                    boolean booked = slot.getBoolean("booked");
                    timeslotList.add(new Timeslot(id, booked));
                }

                Day day = new Day(date, timeslotList);
                _data.add(day);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Server error!", Toast.LENGTH_SHORT).show();
        }
        _calendarAdapter.notifyDataSetChanged();
    }
}
