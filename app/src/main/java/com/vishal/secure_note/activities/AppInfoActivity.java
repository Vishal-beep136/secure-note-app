package com.vishal.secure_note.activities;

import android.os.Bundle;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.vishal.secure_note.databinding.ActivityAppInfoBinding;
import com.vishal.secure_note.util.Constant;

public class AppInfoActivity extends AppCompatActivity {

    ActivityAppInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.infoWebView.setWebViewClient(new WebViewClient());
        binding.infoWebView.getSettings().setAllowContentAccess(true);
        binding.infoWebView.getSettings().setAllowFileAccess(true);

        int infoCode = getIntent().getIntExtra("infoCode", -1);

        if (infoCode != -1) {
            if (infoCode == Constant.PRIVACY_CODE) {
                binding.infoWebView.loadUrl("file:///android_asset/privacy.html");
            } else {
                binding.infoWebView.loadUrl("file:///android_asset/terms.html");
            }
        }

    }


}