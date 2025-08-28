package com.mihir.alzheimerscaregiver.caretaker.data.entity;

import androidx.annotation.NonNull;

public class ReminderEntity {

    public String id;

    @NonNull
    public String title;

    public String description;

    public Long scheduledTimeEpochMillis;

    public boolean isCompleted;

        public ReminderEntity(@NonNull String title,
                                                  String description,
                                                  Long scheduledTimeEpochMillis,
                                                  boolean isCompleted) {
                this.title = title;
                this.description = description;
                this.scheduledTimeEpochMillis = scheduledTimeEpochMillis;
                this.isCompleted = isCompleted;
        }

        // Default constructor for Firebase
        public ReminderEntity() {}
}
