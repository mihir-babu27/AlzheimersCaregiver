package com.mihir.alzheimerscaregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mihir.alzheimerscaregiver.auth.DeviceAuth;

public class SplashGateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", null);

        // First run → collect name
        if (name == null || name.trim().isEmpty()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        // Authenticate with device credentials (PIN/Pattern/Password or biometrics)
        DeviceAuth.prompt(this, new DeviceAuth.Callback() {
            @Override
            public void onSuccess() {
                Intent i = new Intent(SplashGateActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure() {
                finish();
            }
        });
    }
}


