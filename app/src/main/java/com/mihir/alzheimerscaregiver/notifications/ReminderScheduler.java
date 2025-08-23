package com.mihir.alzheimerscaregiver.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class ReminderScheduler {
    private static final String TAG = "ReminderScheduler";
    
    public static void schedule(Context context, long triggerAtMillis, String title, String description) {
        try {
            Log.d(TAG, "Scheduling reminder: " + title + " at " + triggerAtMillis);
            
            // Ensure notification channels are created early
            NotificationUtils.ensureChannels(context);
            
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms, requesting permission");
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                }
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            
            // Use a unique ID for each reminder to avoid conflicts
            int requestCode = (int) (triggerAtMillis % Integer.MAX_VALUE);
            if (requestCode < 0) requestCode = Math.abs(requestCode);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d(TAG, "Reminder scheduled successfully: " + title + " at " + triggerAtMillis);
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder: " + title, e);
        }
    }
    
    public static void cancelReminder(Context context, long triggerAtMillis, String title) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;
            
            Intent intent = new Intent(context, ReminderReceiver.class);
            int requestCode = (int) (triggerAtMillis % Integer.MAX_VALUE);
            if (requestCode < 0) requestCode = Math.abs(requestCode);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Reminder cancelled: " + title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling reminder: " + title, e);
        }
    }
}


