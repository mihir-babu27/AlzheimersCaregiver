package com.mihir.alzheimerscaregiver.caretaker.data.entity;

import androidx.annotation.NonNull;

public class ContactEntity {

    public String id;

    @NonNull
    public String name;

    @NonNull
    public String phoneNumber;

    public String relationship;

    public boolean isPrimary;

    public ContactEntity(@NonNull String name,
                         @NonNull String phoneNumber,
                         String relationship,
                         boolean isPrimary) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }

    // Default constructor for Firebase
    public ContactEntity() {}
}
