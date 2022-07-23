package com.vishal.secure_note.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.vishal.secure_note.R;
import com.vishal.secure_note.databinding.ActivityLoginPasswordBinding;
import com.vishal.secure_note.security.Auth;
import com.vishal.secure_note.util.Common;
import com.vishal.secure_note.util.Constant;

public class LoginPasswordActivity extends AppCompatActivity {

    ActivityLoginPasswordBinding binding;
    private Auth auth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        binding.showPasswordLoginBtn.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePassword();
        });

        binding.passwordLoginBox.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });

        binding.forgotPasswordBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPasswordActivity.this, ResetPasswordActivity.class);
            intent.putExtra("passwordResetCode", auth.getSelectedLoginMethod());
            startActivity(intent);
            finish();
        });

        binding.loginDoneBtn.setOnClickListener(view -> performLogin());
    }

    private void togglePassword() {
        if (isPasswordVisible) {
            binding.passwordLoginBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.showPasswordLoginBtn.setImageResource(R.drawable.ic_eye_open);
        } else {
            binding.passwordLoginBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.showPasswordLoginBtn.setImageResource(R.drawable.ic_eye_close);
        }
        binding.passwordLoginBox.setSelection(binding.passwordLoginBox.getText().length());
    }

    private void performLogin() {
        String password = binding.passwordLoginBox.getText().toString();
        if (password.isEmpty()) {
            setErrorToPasswordBox("Password is required!");
            return;
        }
        int loginMethod = auth.getSelectedLoginMethod();
        if (loginMethod == Constant.APPS_PASSWORD) performAppPasswordLogin();
        else if (loginMethod == Constant.CUSTOM_PASSWORD) performCustomPasswordLogin();
    }

    private void performAppPasswordLogin() {
        try {
            String password = binding.passwordLoginBox.getText().toString();
            boolean isPasswordCorrect = auth.isValidAppPassword(password);
            if (isPasswordCorrect) Common.goToHomeAndFinish(this);
            else {
                setErrorToPasswordBox("App password is wrong!");
                binding.forgotPasswordBtn.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            setErrorToPasswordBox(e.getMessage());
        }
    }

    private void performCustomPasswordLogin() {
        try {
            String password = binding.passwordLoginBox.getText().toString();
            boolean isPasswordCorrect = auth.isValidLoginCustomPassword(password);
            if (isPasswordCorrect) Common.goToHomeAndFinish(this);
            else {
                setErrorToPasswordBox("Custom password is wrong!");
                binding.forgotPasswordBtn.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            setErrorToPasswordBox(e.getMessage());
        }
    }


    private void setErrorToPasswordBox(String msg) {
        binding.passwordLoginBox.setError(msg);
        binding.passwordLoginBox.requestFocus();
    }

}