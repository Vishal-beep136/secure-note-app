package com.vishal.secure_note.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.vishal.secure_note.databinding.ActivityResetPasswordBinding;
import com.vishal.secure_note.security.Auth;
import com.vishal.secure_note.util.Common;
import com.vishal.secure_note.util.Constant;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private int requestResetCode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestResetCode = getIntent().getIntExtra("passwordResetCode", -1);


        binding.checkLastRememberedBtn.setOnClickListener(view -> {
            if (binding.lassRememberedEt.getEditText() == null) return;
            if (binding.lassRememberedEt.getEditText().getText().toString().isEmpty()) {
                binding.lassRememberedEt.setError("Please enter last password!");
                binding.lassRememberedEt.requestFocus();
                return;
            }

            Auth auth = new Auth(this);
            String password;

            if (requestResetCode == Constant.CUSTOM_PASSWORD)
                password = auth.getPassword(Constant.CUSTOM_LOGIN_PASSWORD_KEY);
            else password = auth.getPassword(Constant.APP_PASSWORD_KEY);

            checkLastPasswordRemembered(password);
        });

        binding.resetPasswordBtn.setOnClickListener(view -> handleResetPasswordBtnClicked());

    }

    private void handleResetPasswordBtnClicked() {
        if (binding.resetPasswordEt.getEditText() == null) return;
        if (binding.resetPasswordConfirmEt.getEditText() == null) return;

        String resetPasswordStr = binding.resetPasswordEt.getEditText().getText().toString();
        String confirmationPasswordStr = binding.resetPasswordConfirmEt.getEditText().getText().toString();

        if (resetPasswordStr.isEmpty()) {
            binding.resetPasswordEt.setError("Enter new password");
            binding.resetPasswordEt.requestFocus();
            return;
        }

        if (confirmationPasswordStr.isEmpty()) {
            binding.resetPasswordConfirmEt.setError("Confirm new password");
            binding.resetPasswordConfirmEt.requestFocus();
            return;
        }

        if (!resetPasswordStr.equals(confirmationPasswordStr)) {
            Toast.makeText(this, "Your confirmation password doesn't match", Toast.LENGTH_SHORT).show();
            binding.resetPasswordConfirmEt.setError("Confirmation password not match");
            binding.resetPasswordConfirmEt.requestFocus();
            return;
        }

        resetPassword(requestResetCode, resetPasswordStr);
    }

    private void resetPassword(int requestResetCode, String resetPasswordStr) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (requestResetCode == Constant.APPS_PASSWORD)
                editor.putString("app_password", resetPasswordStr);
            else editor.putString("custom_password", resetPasswordStr);

            editor.apply();
            Toast.makeText(this, "Password Reset Successfully!", Toast.LENGTH_SHORT).show();
            Common.goToHomeAndFinish(this);
        } catch (Exception e) {
            Log.d("VK_E", "resetPassword: error while resetting password  : " + e.getMessage());
        }
    }


    private void checkLastPasswordRemembered(String password) {
        Thread lastRememberedPasswordThread = new Thread(() -> {
            String userEnteredLastRememberPass = Objects.requireNonNull(binding.lassRememberedEt.getEditText()).getText().toString();
            int matchCode = computeLevenshteinDistance(password, userEnteredLastRememberPass);
            runOnUiThread(() -> {
                Log.d("CODEm", "checkLastPasswordRemembered: match code : " + matchCode);
                if (matchCode <= 5) {
                    binding.lastRememberContainer.setVisibility(View.GONE);
                    binding.resetPasswordContainer.setVisibility(View.VISIBLE);
                } else
                    binding.rememberedPassWrongErrorTv.setVisibility(View.VISIBLE);
            });
        });
        lastRememberedPasswordThread.start();
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    private int computeLevenshteinDistance(@NonNull CharSequence lhs, @NonNull CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }

}