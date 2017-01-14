package com.scorpion.sleep.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

/**
 * Created by stephen on 2015-11-24.
 */
public class NetworkManager {

    public static final String LOGIN_URL = "http://104.131.60.15:5050/sign-in";
    public static final String REGISTER_SESSION_URL = "http://104.131.60.15:5050/mobile-login";
    public static final String CALENDAR_URL = "http://104.131.60.15:5050/calender";
    public static final String UPLOAD_URL = "http://104.131.60.15:8090/upload";
    public static final String USERINFO_URL = "http://104.131.60.15:5050/userinfo";
    public static final String MEDHISTORY_URL = "http://104.131.60.15:5050/medhistry";

    private static NetworkManager instance;

    public static NetworkManager inst(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    private RequestQueue _queue;
    private Response.ErrorListener _defaultErrorListener;

    private NetworkManager(Context context) {
        _queue = Volley.newRequestQueue(context.getApplicationContext());
        _defaultErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
            }
        };
    }

    public void submitRequest(Request request) {
        _queue.add(request);
    }

    public Response.ErrorListener getDefaultErrorListener() {
        return _defaultErrorListener;
    }
}
