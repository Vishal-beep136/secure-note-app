package com.vishal.secure_note.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.vishal.secure_note.security.Auth;
import com.vishal.secure_note.util.Common;
import com.vishal.secure_note.util.Constant;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Auth auth = new Auth(this);

        switch (auth.getSelectedLoginMethod()) {
            case Constant.MOBILE_PASSWORD:
                try {
                    auth.loginFromMobilePassword(userPasswordLauncher);
                } catch (Exception e) {
                    Common.goToHomeAndFinish(this);
                }
                break;
            case Constant.APPS_PASSWORD:
                checkPasswordTypeAndRedirect(Constant.APP_PASSWORD_KEY);
                break;
            case Constant.CUSTOM_PASSWORD:
                checkPasswordTypeAndRedirect(Constant.CUSTOM_LOGIN_PASSWORD_KEY);
                break;
        }


    }

    private void checkPasswordTypeAndRedirect(String key) {
        Auth auth = new Auth(this);
        if (auth.getPassword(key).isEmpty()) {
            Common.goToHomeAndFinish(this);
            return;
        }
        startActivity(new Intent(this, LoginPasswordActivity.class));
        finish();
    }

    ActivityResultLauncher<Intent> userPasswordLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Common.goToHomeAndFinish(this);
                } else {
                    Toast.makeText(this, "Password is wrong!", Toast.LENGTH_SHORT).show();
                }
            });
}