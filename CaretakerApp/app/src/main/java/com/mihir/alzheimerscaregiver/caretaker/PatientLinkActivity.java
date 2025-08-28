package com.mihir.alzheimerscaregiver.caretaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class PatientLinkActivity extends AppCompatActivity {

    private EditText patientIdEditText;
    private Button linkButton;
    private TextView descriptionText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_link);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("CaretakerApp", MODE_PRIVATE);

        // Initialize views
        patientIdEditText = findViewById(R.id.patientIdEditText);
        linkButton = findViewById(R.id.linkButton);
        descriptionText = findViewById(R.id.descriptionText);
        progressBar = findViewById(R.id.progressBar);

        // Set description text
        descriptionText.setText(R.string.link_patient_description);

        // Check if user is already linked to a patient
        String linkedPatientId = prefs.getString("linkedPatientId", null);
        if (linkedPatientId != null) {
            // User is already linked, go to main activity
            goToMainActivity();
            return;
        }

        // Set click listener
        linkButton.setOnClickListener(v -> attemptLinkPatient());
    }

    private void attemptLinkPatient() {
        String patientId = patientIdEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(patientId)) {
            patientIdEditText.setError("Patient ID is required");
            patientIdEditText.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        linkButton.setEnabled(false);

        // Create the link in Firestore
        String caretakerUid = mAuth.getCurrentUser().getUid();
        
        // First, check if the patient exists
        db.collection("patients").document(patientId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Patient exists, create the link
                        createCaretakerPatientLink(caretakerUid, patientId);
                    } else {
                        // Patient doesn't exist
                        progressBar.setVisibility(View.GONE);
                        linkButton.setEnabled(true);
                        Toast.makeText(PatientLinkActivity.this, 
                            "Patient ID not found. Please check the ID and try again.", 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createCaretakerPatientLink(String caretakerUid, String patientId) {
        // Create the link document
        Map<String, Object> linkData = new HashMap<>();
        linkData.put("linkedAt", System.currentTimeMillis());
        linkData.put("patientId", patientId);
        linkData.put("caretakerUid", caretakerUid);

        // Add to caretakerPatients collection
        db.collection("caretakerPatients")
                .document(caretakerUid)
                .collection("linkedPatients")
                .document(patientId)
                .set(linkData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Link created successfully
                            Toast.makeText(PatientLinkActivity.this, 
                                R.string.patient_linked_successfully, Toast.LENGTH_SHORT).show();
                            
                            // Save to local preferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("linkedPatientId", patientId);
                            editor.apply();
                            
                            // Go to main activity
                            goToMainActivity();
                        } else {
                            // Link creation failed
                            progressBar.setVisibility(View.GONE);
                            linkButton.setEnabled(true);
                            Toast.makeText(PatientLinkActivity.this, 
                                "Failed to link patient: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(PatientLinkActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
