package com.scorpion.sleep.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scorpion.sleep.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserContext {

    private static final String USERNAME_KEY = "USERNAME_KEY";
    private static final String PASSWORD_KEY = "PASSWORD_KEY";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String UID_KEY = "UID_KEY";
    private static final String SAVE_PW_KEY = "SAVE_PW_KEY";
    private static final String AUTOLOGIN_KEY = "AUTOLOGIN_KEY";

    //TODO, need to remove hack when implemented true login
    public static final String UID = "58c44144dd62294b320de5a5";
    public static final String STEVE_LOCAL_UID = "58c216cd160fe397212f4b3b";
    public static final String STEVE_UID = "58c219d76c934d0651fea371";

    public static final String EMULATOR_LOCAL_API = "http://10.0.2.2:8080/friends/" ;
    public static final String EMULATOR_LOCAL_API_RECOM = "http://10.0.2.2:8080/friends/search/recommend?uid=" ;
    public static final String HW_REMOTE_API = "http://104.131.60.15:8080/friends/" ;
    public static final String HW_REMOTE_API_RECOM = "http://104.131.60.15:8080/friends/search/recommend?uid=" ;
    public static final String HW_REMOTE_API_INVITE = "http://104.131.60.15:8080/friends/pendingfriend?" ;
    public static final String HW_REMOTE_API_ACCEPT = "http://104.131.60.15:8080/friends/befriend?" ;

    private SharedPreferences prefs;
    private Context context;

    private static UserContext instance;

    public static UserContext getUserContext(Context context)
    {
        if (instance == null) {
            instance = new UserContext(context);
        }
        return instance;
    }

    private UserContext(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
    }

    public String getUserName() {
        return prefs.getString(USERNAME_KEY, "");
    }

    public void setUserName(String userName) {
        prefs.edit().putString(USERNAME_KEY, userName).apply();
    }

    public String getUserPassword() {
        return prefs.getString(PASSWORD_KEY, "");
    }

    public void setUserPassword(String userPassword) {
        prefs.edit().putString(PASSWORD_KEY, userPassword).apply();
    }

    public void logout()
    {
        setUserToken("");
        setUserPassword("");
    }

    public String getUserToken() {
        return prefs.getString(TOKEN_KEY, "");
    }

    public void setUserToken(String userToken) {
        prefs.edit().putString(TOKEN_KEY, userToken).commit();
    }

    public void setUID(String id) {
        prefs.edit().putString(UID_KEY, id).commit();
    }

    public String getUID() {
        return prefs.getString(UID_KEY, "-1");
    }

    public boolean isUserLoggedIn() {
        return !getUserToken().equals("");
    }

    public boolean isRememberPassword() {
        return prefs.getBoolean(SAVE_PW_KEY, false);
    }

    public void setRememberPassword(boolean rememberPassword) {
        if (rememberPassword) {
            prefs.edit().putBoolean(SAVE_PW_KEY, true).commit();
        } else {
            prefs.edit().putBoolean(SAVE_PW_KEY, false)
                    .remove(PASSWORD_KEY)
                    .remove(USERNAME_KEY)
                    .commit();
        }
    }

    public boolean isAutoLogIn() {
        return prefs.getBoolean(AUTOLOGIN_KEY, false);
    }

    public void setAutoLogIn(boolean autoLogIn) {
        prefs.edit().putBoolean(AUTOLOGIN_KEY, autoLogIn).commit();
    }

    public void attemptLogin(final String username, final String password, final LoginActivity.LoginResponseListener callback) {
        StringRequest request = new StringRequest(Request.Method.POST, NetworkManager.LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String success = "-1";
                try {
                    success = String.valueOf(parseLoginSuccess(response));
                } catch (JSONException e) {
                    Toast.makeText(context, "Server response error!", Toast.LENGTH_SHORT).show();
                }

                setUID(success);
                if (!success.equals("-1")) {
                    registerSession(callback);
                } else {
                    logout();
                    callback.onLoginFailure();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RESPONSE", "FAIL: " + error.toString());
                Toast.makeText(context, "Network error!", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                if (!getUserToken().equals("")) {
                    params.put("session", getUserToken());
                }
                Log.d("LOGIN-AUTH", "Params for sign-in post");
                printParamsLog(params);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("user-agent", "android");
                Log.d("LOGIN-AUTH", "Headers for sign-in post");
                printParamsLog(params);
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String session = getSessionId(response.headers);
                Log.d("TOKEN", session);
                setUserToken(session);
                return super.parseNetworkResponse(response);
            }

            private String getSessionId(Map<String, String> headers) {
                final String SET_COOKIE = "Set-Cookie";

                // Example response: "connect.sid=s%3A1sX_zaJyQ10D%2B5YI34tC6E0; Path=/; HttpOnly"
                if (headers.containsKey(SET_COOKIE)) {
                    return headers.get(SET_COOKIE).split(";")[0].split("=")[1];
                } else if (headers.containsKey(SET_COOKIE.toLowerCase())) {
                    return headers.get(SET_COOKIE.toLowerCase()).split(";")[0].split("=")[1];
                }
                return "";
            }
        };

        NetworkManager.inst(context.getApplicationContext()).submitRequest(request);
    }

    private int parseLoginSuccess(String response) throws JSONException {
        JSONObject obj = new JSONObject(response);
        return obj.getInt("authenticated");
    }

    /**
     * Called after the login is successful, to store the session in the server
     * Login process is not complete until this completes successfully
     */
    private void registerSession(final LoginActivity.LoginResponseListener callback) {
        StringRequest request = new StringRequest(Request.Method.POST, NetworkManager.REGISTER_SESSION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onLoginSucceed();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onLoginFailure();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", getUID());
                params.put("username", getUserName());
                params.put("session", getUserToken());
                Log.d("LOGIN-AUTH", "Params for mobile-login post");
                printParamsLog(params);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("user-agent", "android");
                Log.d("LOGIN-AUTH", "Headers for mobile-login post");
                printParamsLog(params);
                return params;
            }
        };

        NetworkManager.inst(context.getApplicationContext()).submitRequest(request);
    }

    private void printParamsLog(Map<String, String> params) {
        for (String s : params.keySet()) {
            Log.d("LOGIN-AUTH", s + " : " + params.get(s));
        }
    }
}
