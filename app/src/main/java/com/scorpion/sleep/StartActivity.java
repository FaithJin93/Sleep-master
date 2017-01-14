package com.scorpion.sleep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.scorpion.sleep.Booking.BookingActivity;
import com.scorpion.sleep.Uploading.UploadActivity;
import com.scorpion.sleep.util.UserContext;

public class StartActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        RelativeLayout bookAppointment = (RelativeLayout) findViewById(R.id.button_book_appointment);
        RelativeLayout viewData = (RelativeLayout) findViewById(R.id.button_view_sleep_data);
        RelativeLayout uploadData = (RelativeLayout) findViewById(R.id.button_upload_file);
        RelativeLayout userInfo = (RelativeLayout) findViewById(R.id.button_user_info);
        RelativeLayout needHelp = (RelativeLayout) findViewById(R.id.button_need_help);
        RelativeLayout medHistory = (RelativeLayout) findViewById(R.id.button_medical_history);
        bookAppointment.setOnClickListener(this);
        viewData.setOnClickListener(this);
        uploadData.setOnClickListener(this);
        userInfo.setOnClickListener(this);
        needHelp.setOnClickListener(this);
        medHistory.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                UserContext.getUserContext(getApplicationContext()).logout();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_book_appointment:
                startActivity(new Intent(this, BookingActivity.class));
                break;
            case R.id.button_view_sleep_data:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.button_upload_file:
                startActivity(new Intent(this, UploadActivity.class));
                break;
            case R.id.button_user_info:
                startActivity(new Intent(this, UserInfoActivity.class));
                break;
            case R.id.button_need_help:
                startActivity(new Intent(this, profileActivity.class));
                break;
            case R.id.button_medical_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }
    }
}
