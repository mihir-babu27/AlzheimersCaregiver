package com.mihir.alzheimerscaregiver.caretaker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mihir.alzheimerscaregiver.caretaker.data.entity.ContactEntity;


public class AddEmergencyContactActivity extends AppCompatActivity {

    private EditText contactNameEditText, relationEditText, phoneNumberEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        // Get patient ID from intent
        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        contactNameEditText = findViewById(R.id.contactNameEditText);
        relationEditText = findViewById(R.id.relationEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        saveButton.setOnClickListener(v -> attemptSave());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void attemptSave() {
        String contactName = contactNameEditText.getText().toString().trim();
        String relation = relationEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(contactName)) {
            contactNameEditText.setError("Contact name is required");
            contactNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(relation)) {
            relationEditText.setError("Relation is required");
            relationEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Phone number is required");
            phoneNumberEditText.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Create contact entity
        ContactEntity contact = new ContactEntity(
            contactName,
            phoneNumber,
            relation,
            false // isPrimary
        );

        // Save to Firestore - use 'contacts' collection to match patient app
        db.collection("patients")
                .document(patientId)
                .collection("contacts")
                .add(contact)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentReference> task) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(AddEmergencyContactActivity.this, 
                                R.string.contact_added_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddEmergencyContactActivity.this, 
                                "Failed to add emergency contact: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
