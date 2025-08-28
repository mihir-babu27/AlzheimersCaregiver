package com.mihir.alzheimerscaregiver.caretaker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class NotificationUtils {

    private static final String TAG = "NotificationUtils";
    private static final String CHANNEL_ID = "reminders_channel";
    private static final String TASK_CHANNEL_ID = "tasks_channel";
    private static boolean channelsCreated = false;

    public static void ensureChannels(Context context) {
        if (channelsCreated) return;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    // Create reminders channel
                    NotificationChannel remindersChannel = new NotificationChannel(
                            CHANNEL_ID, 
                            "Reminders", 
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    remindersChannel.setDescription("Reminder notifications for medications and tasks");
                    remindersChannel.enableVibration(true);
                    remindersChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
                    remindersChannel.enableLights(true);
                    manager.createNotificationChannel(remindersChannel);
                    
                    // Create tasks channel
                    NotificationChannel tasksChannel = new NotificationChannel(
                            TASK_CHANNEL_ID, 
                            "Daily Tasks", 
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    tasksChannel.setDescription("Daily task reminders");
                    tasksChannel.enableVibration(true);
                    tasksChannel.setVibrationPattern(new long[]{0, 300, 200, 300});
                    manager.createNotificationChannel(tasksChannel);
                    
                    Log.d(TAG, "Notification channels created successfully");
                }
            }
            channelsCreated = true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating notification channels", e);
        }
    }
}
