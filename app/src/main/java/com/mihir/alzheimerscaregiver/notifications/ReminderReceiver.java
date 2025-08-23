package com.mihir.alzheimerscaregiver.notifications;

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
            
            NotificationUtils.showReminderNotification(context, title, description);
        } catch (Exception e) {
            Log.e(TAG, "Error processing reminder", e);
        }
    }
}


