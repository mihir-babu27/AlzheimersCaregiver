package com.mihir.alzheimerscaregiver.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mihir.alzheimerscaregiver.data.dao.ContactDao;
import com.mihir.alzheimerscaregiver.data.dao.ReminderDao;
import com.mihir.alzheimerscaregiver.data.dao.TaskDao;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;

@Database(
        entities = {
                ReminderEntity.class,
                TaskEntity.class,
                ContactEntity.class
        },
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ReminderDao reminderDao();
    public abstract TaskDao taskDao();
    public abstract ContactDao contactDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "alzheimers_caregiver.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addMigrations(
                                    // Placeholder for future migrations; start at version 1
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Example migration placeholder (from 1 to 2) to illustrate structure
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Implement when bumping DB version
        }
    };
}


