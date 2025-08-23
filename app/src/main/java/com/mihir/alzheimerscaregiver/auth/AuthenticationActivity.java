package com.mihir.alzheimerscaregiver.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mihir.alzheimerscaregiver.MainActivity;
import com.mihir.alzheimerscaregiver.R;

public class AuthenticationActivity extends AppCompatActivity {
    
    private EditText emailEditText, passwordEditText, nameEditText;
    private Button signUpButton, signInButton, toggleModeButton;
    private TextView titleTextView, subtitleTextView;
    private ProgressBar progressBar;
    
    private boolean isSignUpMode = true;
    private FirebaseAuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        
        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager();
        
        // Check if user is already signed in
        if (authManager.isPatientSignedIn()) {
            navigateToMain();
            return;
        }
        
        initializeViews();
        setupClickListeners();
        updateUI();
    }
    
    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInButton);
        toggleModeButton = findViewById(R.id.toggleModeButton);
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> handleSignUp());
        signInButton.setOnClickListener(v -> handleSignIn());
        toggleModeButton.setOnClickListener(v -> toggleMode());
    }
    
    private void toggleMode() {
        isSignUpMode = !isSignUpMode;
        updateUI();
    }
    
    private void updateUI() {
        if (isSignUpMode) {
            titleTextView.setText("Create Patient Account");
            subtitleTextView.setText("Sign up to get started");
            nameEditText.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
            toggleModeButton.setText("Already have an account? Sign In");
        } else {
            titleTextView.setText("Sign In");
            subtitleTextView.setText("Welcome back!");
            nameEditText.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            toggleModeButton.setText("Don't have an account? Sign Up");
        }
    }
    
    private void handleSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        
        if (!validateInputs(email, password, name)) {
            return;
        }
        
        showProgress(true);
        
        authManager.signUpPatient(email, password, name, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String patientId) {
                showProgress(false);
                Toast.makeText(AuthenticationActivity.this, 
                        "Account created successfully! Patient ID: " + patientId, Toast.LENGTH_LONG).show();
                navigateToMain();
            }
            
            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(AuthenticationActivity.this, "Sign up failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void handleSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (!validateInputs(email, password, null)) {
            return;
        }
        
        showProgress(true);
        
        authManager.signInPatient(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String patientId) {
                showProgress(false);
                Toast.makeText(AuthenticationActivity.this, 
                        "Welcome back! Patient ID: " + patientId, Toast.LENGTH_LONG).show();
                navigateToMain();
            }
            
            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(AuthenticationActivity.this, "Sign in failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private boolean validateInputs(String email, String password, String name) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return false;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return false;
        }
        
        if (isSignUpMode && TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return false;
        }
        
        return true;
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!show);
        signInButton.setEnabled(!show);
        toggleModeButton.setEnabled(!show);
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
