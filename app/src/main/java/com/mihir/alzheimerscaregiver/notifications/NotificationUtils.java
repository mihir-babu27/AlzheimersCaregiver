package com.mihir.alzheimerscaregiver.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.mihir.alzheimerscaregiver.R;

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

    public static void showReminderNotification(Context context, String title, String description) {
        try {
            ensureChannels(context);
            
            // Check notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification permission not granted");
                    return;
                }
            }
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_brain)
                    .setContentTitle(title == null ? "Reminder" : title)
                    .setContentText(description == null ? "It's time for your reminder" : description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
            
            try {
                NotificationManagerCompat.from(context).notify(
                        (int) System.currentTimeMillis(), 
                        builder.build()
                );
                Log.d(TAG, "Reminder notification displayed successfully: " + title);
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when showing notification", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing reminder notification", e);
        }
    }

    public static void showReminderNotificationWithIntent(Context context, String title, String description, android.app.PendingIntent contentIntent) {
        try {
            ensureChannels(context);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification permission not granted");
                    return;
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_brain)
                    .setContentTitle(title == null ? "Reminder" : title)
                    .setContentText(description == null ? "It's time for your reminder" : description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

            try {
                NotificationManagerCompat.from(context).notify(
                        (int) System.currentTimeMillis(),
                        builder.build()
                );
                Log.d(TAG, "Reminder notification with intent displayed successfully: " + title);
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when showing notification with intent", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing reminder notification with intent", e);
        }
    }
    
    public static void showTaskNotification(Context context, String title, String description) {
        try {
            ensureChannels(context);
            
            // Check notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification permission not granted");
                    return;
                }
            }
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TASK_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_tasks)
                    .setContentTitle(title == null ? "Daily Task" : title)
                    .setContentText(description == null ? "You have a task to complete" : description)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 300, 200, 300})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
            
            try {
                NotificationManagerCompat.from(context).notify(
                        (int) System.currentTimeMillis(), 
                        builder.build()
                );
                Log.d(TAG, "Task notification displayed successfully: " + title);
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when showing notification", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing task notification", e);
        }
    }
}


