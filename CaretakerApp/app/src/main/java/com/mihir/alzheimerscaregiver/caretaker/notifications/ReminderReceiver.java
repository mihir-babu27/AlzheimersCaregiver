package com.mihir.alzheimerscaregiver.caretaker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            
            Log.d(TAG, "Reminder received: " + title + " - " + description);
            
            // Show notification using the patient app's notification system
            // This will be handled by the patient app when it receives the data
            Log.d(TAG, "Reminder notification should be shown by patient app for: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error processing reminder", e);
        }
    }
}
