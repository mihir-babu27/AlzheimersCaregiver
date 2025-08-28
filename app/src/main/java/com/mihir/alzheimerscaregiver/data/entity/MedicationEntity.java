package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.NonNull;

public class MedicationEntity {

    public String id;

    @NonNull
    public String name;

    @NonNull
    public String dosage;

    @NonNull
    public String time;

    public String createdBy;

    public Long createdAt;

    public boolean isActive;

    public MedicationEntity(@NonNull String name,
                           @NonNull String dosage,
                           @NonNull String time,
                           String createdBy,
                           Long createdAt,
                           boolean isActive) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Default constructor for Firebase
    public MedicationEntity() {}
}
