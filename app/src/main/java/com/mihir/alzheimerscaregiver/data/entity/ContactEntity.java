package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "contacts",
        indices = {
                @Index(value = {"phone_number"}, unique = true),
                @Index(value = {"is_primary"})
        }
)
public class ContactEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @NonNull
    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "relationship")
    public String relationship;

    @ColumnInfo(name = "is_primary")
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
}


