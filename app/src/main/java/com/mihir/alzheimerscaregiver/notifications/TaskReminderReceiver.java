package com.mihir.alzheimerscaregiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        if (title == null) title = "Task due";
        NotificationUtils.showReminderNotification(context, title, description);
    }
}


