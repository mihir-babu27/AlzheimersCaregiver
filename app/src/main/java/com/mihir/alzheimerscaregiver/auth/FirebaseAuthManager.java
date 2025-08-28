package com.mihir.alzheimerscaregiver.auth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.PatientEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

public class FirebaseAuthManager {
    
    private static final String TAG = "FirebaseAuthManager";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    
    public FirebaseAuthManager() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseConfig.getInstance();
    }
    
    /**
     * Sign up a new patient with email and password
     */
    public void signUpPatient(String email, String password, String name, AuthCallback callback) {
        if (email == null || password == null || name == null) {
            callback.onError("Email, password, and name are required");
            return;
        }
        
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Create patient document in Firestore
                            createPatientDocument(user.getUid(), name, email, callback);
                        } else {
                            callback.onError("Failed to get user after sign up");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Sign up failed";
                        callback.onError(errorMessage);
                    }
                });
    }
    
    /**
     * Sign in existing patient
     */
    public void signInPatient(String email, String password, AuthCallback callback) {
        if (email == null || password == null) {
            callback.onError("Email and password are required");
            return;
        }
        
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess(user.getUid());
                        } else {
                            callback.onError("Failed to get user after sign in");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Sign in failed";
                        callback.onError(errorMessage);
                    }
                });
    }
    
    /**
     * Sign out current patient
     */
    public void signOut() {
        auth.signOut();
    }
    
    /**
     * Get current signed-in patient
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    /**
     * Check if patient is currently signed in
     */
    public boolean isPatientSignedIn() {
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Get current patient's UID
     */
    public String getCurrentPatientId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Create patient document in Firestore after successful sign up
     */
    private void createPatientDocument(String patientId, String name, String email, AuthCallback callback) {
        PatientEntity patient = new PatientEntity(patientId, name, email);
        
        db.collection("patients").document(patientId)
                .set(patient)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Patient document created successfully");
                        callback.onSuccess(patientId);
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to create patient document";
                        Log.e(TAG, "Error creating patient document: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
    }
    
    /**
     * Get patient data from Firestore
     */
    public void getPatientData(String patientId, PatientDataCallback callback) {
        db.collection("patients").document(patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        PatientEntity patient = task.getResult().toObject(PatientEntity.class);
                        if (patient != null) {
                            callback.onSuccess(patient);
                        } else {
                            callback.onError("Failed to parse patient data");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to get patient data";
                        callback.onError(errorMessage);
                    }
                });
    }
    
    /**
     * Update patient data in Firestore
     */
    public void updatePatientData(PatientEntity patient, PatientDataCallback callback) {
        db.collection("patients").document(patient.patientId)
                .set(patient)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(patient);
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to update patient data";
                        callback.onError(errorMessage);
                    }
                });
    }
    
    /**
     * Add caretaker to patient's caretakerIds list
     */
    public void addCaretaker(String patientId, String caretakerId, PatientDataCallback callback) {
        db.collection("patients").document(patientId)
                .update("caretakerIds", java.util.Arrays.asList(caretakerId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Refresh patient data
                        getPatientData(patientId, callback);
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to add caretaker";
                        callback.onError(errorMessage);
                    }
                });
    }
    
    public interface AuthCallback {
        void onSuccess(String patientId);
        void onError(String error);
    }
    
    public interface PatientDataCallback {
        void onSuccess(PatientEntity patient);
        void onError(String error);
    }
}
