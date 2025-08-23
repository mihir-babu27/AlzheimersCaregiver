package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.List;

public class PatientEntity {
    
    public String patientId; // FirebaseAuth UID
    public String name;
    public String email;
    public List<String> caretakerIds;
    public Timestamp createdAt;
    
    // Default constructor for Firebase
    public PatientEntity() {}
    
    public PatientEntity(@NonNull String patientId, @NonNull String name, @NonNull String email) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.caretakerIds = new java.util.ArrayList<>();
        this.createdAt = Timestamp.now();
    }
    
    public PatientEntity(@NonNull String patientId, @NonNull String name, @NonNull String email, 
                        List<String> caretakerIds, Timestamp createdAt) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.caretakerIds = caretakerIds != null ? caretakerIds : new java.util.ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }
}
