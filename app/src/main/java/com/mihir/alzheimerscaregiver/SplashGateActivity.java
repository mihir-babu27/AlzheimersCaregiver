package com.mihir.alzheimerscaregiver;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mihir.alzheimerscaregiver.auth.FirebaseAuthManager;

public class SplashGateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase early to prevent initialization issues
        try {
            com.mihir.alzheimerscaregiver.data.FirebaseInitializer.initialize(this);
        } catch (Exception e) {
            android.util.Log.e("SplashGateActivity", "Error initializing Firebase", e);
            // Continue with app initialization even if Firebase fails
        }

        // Initialize notification channels early
        com.mihir.alzheimerscaregiver.notifications.NotificationUtils.ensureChannels(this);

        // Initialize Firebase Auth Manager
        FirebaseAuthManager authManager = new FirebaseAuthManager();

        // Check if user is already signed in
        if (authManager.isPatientSignedIn()) {
            // User is signed in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // User is not signed in, go to AuthenticationActivity
            startActivity(new Intent(this, com.mihir.alzheimerscaregiver.auth.AuthenticationActivity.class));
            finish();
        }
    }
}


