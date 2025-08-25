package com.mihir.alzheimerscaregiver.caretaker.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * This service is designed to be used by the patient app to schedule notifications
 * for reminders and tasks created by the caretaker app.
 * 
 * The patient app should call this service whenever it detects new data in Firestore
 * that needs notification scheduling.
 */
public class PatientNotificationService {
    private static final String TAG = "PatientNotificationService";
    
    /**
     * Schedule a medication reminder notification on the patient's device
     * @param context The patient app's context
     * @param triggerAtMillis When to show the notification
     * @param title The medication title (e.g., "Donepezil - 10mg")
     * @param description The medication description (e.g., "Take at 08:00 AM")
     */
    public static void scheduleMedicationReminder(Context context, long triggerAtMillis, String title, String description) {
        try {
            Log.d(TAG, "Scheduling medication reminder: " + title + " at " + triggerAtMillis);
            
            // Ensure notification channels are created
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
            intent.putExtra("type", "medication");
            
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
            Log.d(TAG, "Medication reminder scheduled successfully: " + title + " at " + triggerAtMillis);
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling medication reminder: " + title, e);
        }
    }
    
    /**
     * Schedule a task reminder notification on the patient's device
     * @param context The patient app's context
     * @param triggerAtMillis When to show the notification
     * @param title The task title (e.g., "Go for a walk")
     * @param description The task description (e.g., "30 minutes in the garden")
     */
    public static void scheduleTaskReminder(Context context, long triggerAtMillis, String title, String description) {
        try {
            Log.d(TAG, "Scheduling task reminder: " + title + " at " + triggerAtMillis);
            
            // Ensure notification channels are created
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

            Intent intent = new Intent(context, TaskReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("type", "task");
            
            // Use a unique ID for each task to avoid conflicts
            int requestCode = (int) (triggerAtMillis % Integer.MAX_VALUE);
            if (requestCode < 0) requestCode = Math.abs(requestCode);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d(TAG, "Task reminder scheduled successfully: " + title + " at " + triggerAtMillis);
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling task reminder: " + title, e);
        }
    }
    
    /**
     * Cancel a scheduled reminder
     * @param context The patient app's context
     * @param triggerAtMillis The scheduled time to identify the reminder
     * @param title The reminder title for logging
     */
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
    
    /**
     * Cancel a scheduled task
     * @param context The patient app's context
     * @param triggerAtMillis The scheduled time to identify the task
     * @param title The task title for logging
     */
    public static void cancelTask(Context context, long triggerAtMillis, String title) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;
            
            Intent intent = new Intent(context, TaskReminderReceiver.class);
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
                Log.d(TAG, "Task cancelled: " + title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling task: " + title, e);
        }
    }
}
