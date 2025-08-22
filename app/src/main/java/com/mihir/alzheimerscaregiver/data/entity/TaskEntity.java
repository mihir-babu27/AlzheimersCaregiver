package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tasks",
        indices = {
                @Index(value = {"is_completed"}),
                @Index(value = {"category"}),
                @Index(value = {"scheduled_time_epoch_millis"})
        }
)
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    @ColumnInfo(name = "category")
    public String category; // e.g., Morning, Exercise, Cognitive, etc.

    @ColumnInfo(name = "scheduled_time_epoch_millis")
    public Long scheduledTimeEpochMillis; // nullable if unscheduled

    @ColumnInfo(name = "is_recurring")
    public boolean isRecurring;

    @ColumnInfo(name = "recurrence_rule")
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
}


