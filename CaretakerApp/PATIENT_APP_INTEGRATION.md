# Patient App Notification Integration Guide

## Overview
This guide explains how to integrate the notification system into your existing Alzheimer's Caregiver patient app so that notifications for reminders and tasks created by the CaretakerApp appear on the patient's device at the scheduled times.

## The Problem
- **CaretakerApp** runs on the caretaker's device and creates data in Firestore
- **Patient App** reads the data but doesn't automatically schedule notifications
- **Result**: Notifications appear on the wrong device (caretaker's) instead of the patient's

## The Solution
The patient app needs to implement **real-time listeners** that automatically detect new data from the CaretakerApp and schedule notifications locally on the patient's device.

## Step 1: Copy Notification Classes
Copy these notification classes from the CaretakerApp to your patient app:

### Required Files:
1. `NotificationUtils.java` - Creates notification channels
2. `ReminderReceiver.java` - Handles medication reminder broadcasts
3. `TaskReminderReceiver.java` - Handles task reminder broadcasts
4. `PatientNotificationService.java` - Schedules notifications

### Package Structure:
```
com.mihir.alzheimerscaregiver.notifications/
├── NotificationUtils.java
├── ReminderReceiver.java
├── TaskReminderReceiver.java
└── PatientNotificationService.java
```

## Step 2: Update AndroidManifest.xml
Add these permissions and receivers to your patient app's manifest:

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Add these receivers inside the <application> tag -->
<receiver
    android:name=".notifications.ReminderReceiver"
    android:exported="false" />
    
<receiver
    android:name=".notifications.TaskReminderReceiver"
    android:exported="false" />
```

## Step 3: Implement Real-Time Firestore Listeners
Add real-time listeners in your patient app to detect new data and schedule notifications:

### For Medication Reminders:
```java
// In your MainActivity or wherever you load reminders
private void setupRemindersListener() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String patientId = getCurrentPatientId(); // Get from your auth system
    
    db.collection("patients")
        .document(patientId)
        .collection("reminders")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("RemindersListener", "Error listening for reminders", error);
                    return;
                }
                
                if (value != null) {
                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            // New reminder added - schedule notification
                            DocumentSnapshot doc = change.getDocument();
                            scheduleReminderNotification(doc);
                        }
                    }
                }
            }
        });
}

private void scheduleReminderNotification(DocumentSnapshot doc) {
    try {
        String title = doc.getString("title");
        String description = doc.getString("description");
        Long scheduledTime = doc.getLong("scheduledTimeEpochMillis");
        
        if (title != null && description != null && scheduledTime != null) {
            // Schedule notification using the service
            PatientNotificationService.scheduleMedicationReminder(
                this, scheduledTime, title, description
            );
            Log.d("MainActivity", "Scheduled notification for reminder: " + title);
        }
    } catch (Exception e) {
        Log.e("MainActivity", "Error scheduling reminder notification", e);
    }
}
```

### For Tasks:
```java
// In your MainActivity or wherever you load tasks
private void setupTasksListener() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String patientId = getCurrentPatientId(); // Get from your auth system
    
    db.collection("patients")
        .document(patientId)
        .collection("tasks")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("TasksListener", "Error listening for tasks", error);
                    return;
                }
                
                if (value != null) {
                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            // New task added - schedule notification
                            DocumentSnapshot doc = change.getDocument();
                            scheduleTaskNotification(doc);
                        }
                    }
                }
            }
        });
}

private void scheduleTaskNotification(DocumentSnapshot doc) {
    try {
        String name = doc.getString("name");
        String description = doc.getString("description");
        Long scheduledTime = doc.getLong("scheduledTimeEpochMillis");
        
        if (name != null && description != null && scheduledTime != null) {
            // Schedule notification using the service
            PatientNotificationService.scheduleTaskReminder(
                this, scheduledTime, name, description
            );
            Log.d("MainActivity", "Scheduled notification for task: " + name);
        }
    } catch (Exception e) {
        Log.e("MainActivity", "Error scheduling task notification", e);
    }
}
```

## Step 4: Initialize Notification Channels
Call this in your MainActivity's onCreate:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // Initialize notification channels
    NotificationUtils.ensureChannels(this);
    
    // Setup real-time listeners
    setupRemindersListener();
    setupTasksListener();
    
    // ... rest of your initialization code
}
```

## Step 5: Handle Notification Clicks
Update your ReminderReceiver and TaskReminderReceiver to show actual notifications:

### ReminderReceiver.java:
```java
@Override
public void onReceive(Context context, Intent intent) {
    String title = intent.getStringExtra("title");
    String description = intent.getStringExtra("description");
    
    // Show actual notification
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminders_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(description)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true);
    
    NotificationManager manager = context.getSystemService(NotificationManager.class);
    if (manager != null) {
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
```

### TaskReminderReceiver.java:
```java
@Override
public void onReceive(Context context, Intent intent) {
    String title = intent.getStringExtra("title");
    String description = intent.getStringExtra("description");
    
    // Show actual notification
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "tasks_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(description)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true);
    
    NotificationManager manager = context.getSystemService(NotificationManager.class);
    if (manager != null) {
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
```

## How It Works

1. **CaretakerApp** creates a reminder/task in Firestore with `scheduledTimeEpochMillis`
2. **Patient App** detects the new data via real-time Firestore listener
3. **Patient App** calls `PatientNotificationService.scheduleMedicationReminder()` or `scheduleTaskReminder()`
4. **Patient App** schedules an alarm using `AlarmManager` for the specified time
5. **Patient App** shows the notification when the alarm triggers

## Testing

1. **Build and install** the CaretakerApp on the caretaker's device
2. **Build and install** the updated patient app on the patient's device
3. **Create a reminder/task** using the CaretakerApp
4. **Verify** the notification appears on the patient's device at the scheduled time

## Troubleshooting

### Notifications not appearing:
- Check that notification channels are created
- Verify permissions are granted
- Check logcat for error messages
- Ensure the patient app is running or has background permissions

### Wrong notification times:
- Verify `scheduledTimeEpochMillis` is correctly set
- Check timezone handling
- Ensure the timestamp is in the future

### Permission issues:
- Request notification permissions on Android 13+
- Request exact alarm permissions on Android 12+
- Check that all required permissions are granted

## Security Notes

- The patient app only reads from their own `patients/{patientId}` document
- Caretakers can only write if linked via `caretakerPatients/{caretakerUid}/linkedPatients/{patientId}`
- All data is validated and sanitized before scheduling notifications

## Future Enhancements

- Add notification sound customization
- Implement recurring notifications
- Add notification history
- Support for multiple notification channels
- Integration with Android's Do Not Disturb settings
