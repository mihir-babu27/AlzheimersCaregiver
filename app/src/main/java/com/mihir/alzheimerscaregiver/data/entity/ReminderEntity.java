package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminders",
        indices = {
                @Index(value = {"scheduled_time_epoch_millis"}),
                @Index(value = {"is_completed"})
        }
)
public class ReminderEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "scheduled_time_epoch_millis")
    public Long scheduledTimeEpochMillis;

    @ColumnInfo(name = "is_completed")
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
}


