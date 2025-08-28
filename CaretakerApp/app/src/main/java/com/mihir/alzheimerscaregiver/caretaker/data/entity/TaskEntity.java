package com.mihir.alzheimerscaregiver.caretaker.data.entity;

import androidx.annotation.NonNull;

public class TaskEntity {

    public String id;

    @NonNull
    public String name;

    public String description;

    public boolean isCompleted;

    public String category; // e.g., Morning, Exercise, Cognitive, etc.

    public Long scheduledTimeEpochMillis; // nullable if unscheduled

    public boolean isRecurring;

    public String recurrenceRule; // e.g., DAILY, WEEKLY:MON,WED,FRI

    public TaskEntity(@NonNull String name,
                      String description,
                      boolean isCompleted,
                      String category,
                      Long scheduledTimeEpochMillis,
                      boolean isRecurring,
                      String recurrenceRule) {
        this.name = name;
        this.description = description;
        this.isCompleted = isCompleted;
        this.category = category;
        this.scheduledTimeEpochMillis = scheduledTimeEpochMillis;
        this.isRecurring = isRecurring;
        this.recurrenceRule = recurrenceRule;
    }

    // Default constructor for Firebase
    public TaskEntity() {}
}
