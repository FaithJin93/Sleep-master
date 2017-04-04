package com.scorpion.sleep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.scorpion.sleep.util.UserContext;

public class LoginActivity extends Activity implements OnClickListener {
	
	private EditText userName, password;  
    private CheckBox rem_pw, auto_login;  
    private Button btn_login;  
    private String userNameValue,passwordValue;  
    private Context context;

    public interface LoginResponseListener {
        void onLoginSucceed(String uid);
        void onLoginFailure();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.logoin);
		context = this;

		//
        userName = (EditText) findViewById(R.id.et_zh);
        password = (EditText) findViewById(R.id.et_mima);  
        rem_pw = (CheckBox) findViewById(R.id.cb_mima);  
        auto_login = (CheckBox) findViewById(R.id.cb_auto);  
        btn_login = (Button) findViewById(R.id.btn_login);


        final UserContext userContext = UserContext.getUserContext(getApplicationContext());
        if (userContext.isRememberPassword()) {
            rem_pw.setChecked(true);
            userName.setText(userContext.getUserName());
            password.setText(userContext.getUserPassword());
        }
        if (userContext.isAutoLogIn()) {
            auto_login.setChecked(true);
            if (userContext.isUserLoggedIn()) {
                Intent intent = new Intent(this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        }
        
     //
        btn_login.setOnClickListener(this);
  

        // 监听记住密码多选框按钮事件
        rem_pw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {  
                if (rem_pw.isChecked()) {  
                      
                    System.out.println("记住密码已选中");
                    userContext.setRememberPassword(true);
                      
                } else {
                      
                    System.out.println("记住密码没有选中");
                    userContext.setRememberPassword(false);
                      
                }  
  
            }  
        });  
        
        //监听自动登录多选框事件
        auto_login.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (auto_login.isChecked()) {
                    System.out.println("自动登录已选中");
                    userContext.setAutoLogIn(true);

                } else {
                    System.out.println("自动登录没有选中");
                    userContext.setAutoLogIn(false);
                }
            }
        });
    }

        public void onClick(final View v) {
            final UserContext userContext = UserContext.getUserContext(getApplicationContext());

            v.setEnabled(false);
            userNameValue = userName.getText().toString();
            passwordValue = password.getText().toString();




            userContext.attemptLogin(userNameValue, passwordValue, new LoginResponseListener() {

                @Override
                public void onLoginSucceed(String uid) {
                    Log.d("LOGIN", "Login succeeded!");
                    /*
                    Toast.makeText(context, "Login Succeed", Toast.LENGTH_SHORT).show();
                    if(rem_pw.isChecked()) {
                        userContext.setUserName(userNameValue);
                        userContext.setUserPassword(passwordValue);
                    }
                    Intent intent = new Intent(context, StartActivity.class);
                    startActivity(intent);
                    finish();
                    */
                    userContext.setUID(uid);
                    Intent intent = new Intent(context, StartActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onLoginFailure() {
                    v.setEnabled(true);
                    Log.d("LOGIN", "Login failed!");
                    Toast.makeText(context, "UserName do not exist", Toast.LENGTH_LONG).show();
                }
            });

        }

}
