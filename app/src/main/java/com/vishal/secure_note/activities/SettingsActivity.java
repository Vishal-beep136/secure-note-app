package com.vishal.secure_note.activities;

import static com.vishal.secure_note.util.Constant.APPS_PASSWORD;
import static com.vishal.secure_note.util.Constant.APP_PASSWORD_KEY;
import static com.vishal.secure_note.util.Constant.APP_PASSWORD_SET_REQ_CODE;
import static com.vishal.secure_note.util.Constant.CUSTOM_PASSWORD;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.vishal.secure_note.R;
import com.vishal.secure_note.databinding.SettingsActivityBinding;
import com.vishal.secure_note.util.Constant;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private int reqCodeForAppPassword;

    SettingsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reqCodeForAppPassword = getIntent().getIntExtra("requestSetPassword", 0);
        binding.backBtnSettings.setOnClickListener(view -> finish());
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (key.equals(APP_PASSWORD_KEY)) {
            //here APP_PASSWORD_KEY value changes
            if (reqCodeForAppPassword == APP_PASSWORD_SET_REQ_CODE) {
                Intent intent = new Intent();
                intent.putExtra("isPasswordSet", true);
                setResult(RESULT_OK, intent);
                finish();
            }

        }
    };

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference customPasswordEtPref = findPreference("custom_password");
            EditTextPreference appPasswordEt = findPreference("app_password");
            Preference changeAppPassword = findPreference("change_app_password");
            Preference changeCustomPassword = findPreference("change_custom_password");

            startActivityForToChangePassword(changeAppPassword, changeCustomPassword);

            showSetCustomPasswordSettingIfTypeIsCustom(customPasswordEtPref);

            showPasswordPreferenceSummery(customPasswordEtPref, changeCustomPassword, "custom password");
            showPasswordPreferenceSummery(appPasswordEt, changeAppPassword, "app password");

            hideOriginalEtAndShowPreferenceOnly(appPasswordEt, changeAppPassword);
            hideOriginalEtAndShowPreferenceOnly(customPasswordEtPref, changeCustomPassword);


            Preference privacyPreference = findPreference("privacy_preference");
            Preference termsOfUsePreference = findPreference("terms_of_use_preference");

            if (privacyPreference != null) {
                privacyPreference.setOnPreferenceClickListener(preference -> {
                    startActivityToAppInfo(Constant.PRIVACY_CODE);
                    return false;
                });
            }

            if (termsOfUsePreference != null) {
                termsOfUsePreference.setOnPreferenceClickListener(preference -> {
                    startActivityToAppInfo(Constant.TERMS_OF_USE_CODE);
                    return false;
                });
            }


        }

        private void startActivityToAppInfo(int code) {
            Intent intent = new Intent(getContext(), AppInfoActivity.class);
            intent.putExtra("infoCode", code);
            startActivity(intent);
        }

        private void startActivityForToChangePassword(Preference changeAppPassword, Preference changeCustomPassword) {
            if (changeAppPassword == null) return;
            if (changeCustomPassword == null) return;

            changeCustomPassword.setOnPreferenceClickListener(preference -> {
                startResetActivity(CUSTOM_PASSWORD);
                return false;
            });

            changeAppPassword.setOnPreferenceClickListener(preference -> {
                startResetActivity(APPS_PASSWORD);
                return false;
            });
        }

        private void startResetActivity(int passwordToResetCode) {
            Intent intent = new Intent(getContext(), ResetPasswordActivity.class);
            intent.putExtra("passwordResetCode", passwordToResetCode);
            startActivity(intent);
        }

        private void showPasswordPreferenceSummery(EditTextPreference passwordEtPref, Preference preferenceSummery, String passwordName) {
            if (passwordEtPref == null) return;
            if (preferenceSummery == null) return;

            preferenceSummery.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> {
                String passwordEditStr = passwordEtPref.getText();
                if (passwordEditStr == null || TextUtils.isEmpty(passwordEditStr))
                    return "No " + passwordName + " set";

                return "Your " + passwordName + " is " + showOnlyLast3Char(passwordEditStr);
            });
        }

        private void showSetCustomPasswordSettingIfTypeIsCustom(EditTextPreference customPasswordEtPref) {
            ListPreference passwordOpenTypePrefList = findPreference("password_method");
            String customPasswordType = "2";

            if (customPasswordEtPref == null) return;

            if (passwordOpenTypePrefList != null) {
                passwordOpenTypePrefList.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (customPasswordEtPref.getText() == null)
                        customPasswordEtPref.setVisible(newValue.equals(customPasswordType));
                    return true;
                });
            }
        }

        private void hideOriginalEtAndShowPreferenceOnly(EditTextPreference originalEt, Preference preference) {
            if (originalEt == null) return;
            if (preference == null) return;

            String originalTxt = originalEt.getText();

            if (originalTxt == null) return;
            if (!originalTxt.isEmpty()) {
                originalEt.setVisible(false);
                preference.setVisible(true);
            }
        }

        @NonNull
        private String showOnlyLast3Char(@NonNull String str) {
            if (str.length() <= 3) return getAsterisks(str.length());
            final int charToShow = 3;
            String asterisksForPassword = getAsterisks(str.length());
            return asterisksForPassword + str.substring(str.length() - charToShow);
        }

        @NonNull
        private String getAsterisks(int asteriskCount) {
            StringBuilder asterisk = new StringBuilder();
            for (int i = 0; i < asteriskCount; i++) asterisk.append("*");
            return asterisk.toString();
        }

    }
}