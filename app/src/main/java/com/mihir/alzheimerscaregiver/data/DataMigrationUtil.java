package com.mihir.alzheimerscaregiver.data;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;

import java.util.List;

/**
 * Utility class to help migrate data from Room database to Firebase Firestore
 * This should be used only once during the migration process
 */
public class DataMigrationUtil {
    
    private static final String TAG = "DataMigrationUtil";
    private final FirebaseFirestore db;
    
    public DataMigrationUtil() {
        this.db = FirebaseConfig.getInstance();
    }
    
    /**
     * Migrate contacts data to Firebase
     */
    public void migrateContacts(List<ContactEntity> contacts, MigrationCallback callback) {
        if (contacts == null || contacts.isEmpty()) {
            callback.onSuccess("No contacts to migrate");
            return;
        }
        
        int totalContacts = contacts.size();
        final int[] migratedCount = {0};
        
        for (ContactEntity contact : contacts) {
            db.collection("contacts").document(contact.id).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        migratedCount[0]++;
                        Log.d(TAG, "Migrated contact: " + contact.name);
                        
                        if (migratedCount[0] == totalContacts) {
                            callback.onSuccess("Successfully migrated " + totalContacts + " contacts");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to migrate contact: " + contact.name, e);
                        callback.onError("Failed to migrate contact: " + contact.name + " - " + e.getMessage());
                    });
        }
    }
    
    /**
     * Migrate tasks data to Firebase
     */
    public void migrateTasks(List<TaskEntity> tasks, MigrationCallback callback) {
        if (tasks == null || tasks.isEmpty()) {
            callback.onSuccess("No tasks to migrate");
            return;
        }
        
        int totalTasks = tasks.size();
        final int[] migratedCount = {0};
        
        for (TaskEntity task : tasks) {
            db.collection("tasks").document(task.id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        migratedCount[0]++;
                        Log.d(TAG, "Migrated task: " + task.name);
                        
                        if (migratedCount[0] == totalTasks) {
                            callback.onSuccess("Successfully migrated " + totalTasks + " tasks");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to migrate task: " + task.name, e);
                        callback.onError("Failed to migrate task: " + task.name + " - " + e.getMessage());
                    });
        }
    }
    
    /**
     * Migrate reminders data to Firebase
     */
    public void migrateReminders(List<ReminderEntity> reminders, MigrationCallback callback) {
        if (reminders == null || reminders.isEmpty()) {
            callback.onSuccess("No reminders to migrate");
            return;
        }
        
        int totalReminders = reminders.size();
        final int[] migratedCount = {0};
        
        for (ReminderEntity reminder : reminders) {
            db.collection("reminders").document(reminder.id).set(reminder)
                    .addOnSuccessListener(aVoid -> {
                        migratedCount[0]++;
                        Log.d(TAG, "Migrated reminder: " + reminder.title);
                        
                        if (migratedCount[0] == totalReminders) {
                            callback.onSuccess("Successfully migrated " + totalReminders + " reminders");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to migrate reminder: " + reminder.title, e);
                        callback.onError("Failed to migrate reminder: " + reminder.title + " - " + e.getMessage());
                    });
        }
    }
    
    /**
     * Check if data exists in Firebase collections
     */
    public void checkFirebaseDataStatus(DataStatusCallback callback) {
        db.collection("contacts").get()
                .addOnSuccessListener(contactsSnapshot -> {
                    db.collection("tasks").get()
                            .addOnSuccessListener(tasksSnapshot -> {
                                db.collection("reminders").get()
                                        .addOnSuccessListener(remindersSnapshot -> {
                                            callback.onStatusChecked(
                                                    contactsSnapshot.size(),
                                                    tasksSnapshot.size(),
                                                    remindersSnapshot.size()
                                            );
                                        })
                                        .addOnFailureListener(e -> callback.onError("Failed to check reminders: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to check tasks: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Failed to check contacts: " + e.getMessage()));
    }
    
    public interface MigrationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface DataStatusCallback {
        void onStatusChecked(int contactsCount, int tasksCount, int remindersCount);
        void onError(String error);
    }
}
