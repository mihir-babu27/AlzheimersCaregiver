package com.mihir.alzheimerscaregiver.caretaker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaskReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "TaskReminderReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            
            Log.d(TAG, "Task reminder received: " + title + " - " + description);
            
            // Show notification using the patient app's notification system
            // This will be handled by the patient app when it receives the data
            Log.d(TAG, "Task notification should be shown by patient app for: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error processing task reminder", e);
        }
        }
}
