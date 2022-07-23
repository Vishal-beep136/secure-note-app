package com.vishal.secure_note.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.activity.result.ActivityResultLauncher;

import com.vishal.secure_note.util.Constant;

/**
 * Created by Vishal on 18, Jul, 2022
 */

public class Auth {
    private final Context context;

    public Auth(Context context) {
        this.context = context;
    }

    public int getSelectedLoginMethod() {
        SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedMethod = appPref.getString(Constant.PASSWORD_METHOD_KEY, "0");
        return Integer.parseInt(selectedMethod);
    }

    public void loginFromMobilePassword(ActivityResultLauncher<Intent> userPasswordLauncher) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            Intent credentialsIntent = keyguardManager.createConfirmDeviceCredentialIntent("Password Required", "Please enter your password for security");
            if (credentialsIntent != null) userPasswordLauncher.launch(credentialsIntent);
            else throw new NullPointerException("Keyguard Manager Not available. Intent is null");
        } else
            throw new UnsupportedOperationException("In this device keyguard manager is not available!");
    }

    public String getPassword(String passwordKey) {
        SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(context);
        return appPref.getString(passwordKey, "");
    }

    public boolean isValidAppPassword(String password) {
        String appPassword = getPassword(Constant.APP_PASSWORD_KEY);
        if (appPassword.isEmpty())
            throw new IllegalArgumentException("App password is not set.");
        return password.equalsIgnoreCase(appPassword);
    }

    public boolean isValidLoginCustomPassword(String password) {
        String customPassword = getPassword(Constant.CUSTOM_LOGIN_PASSWORD_KEY);
        if (customPassword.isEmpty())
            throw new IllegalArgumentException("Custom password is not set.");
        return customPassword.equalsIgnoreCase(password);
    }
}
