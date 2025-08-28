package com.mihir.alzheimerscaregiver.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mihir.alzheimerscaregiver.MmseQuizActivity;

public class MmseReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent openIntent = new Intent(context, MmseQuizActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                2001,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationUtils.showReminderNotificationWithIntent(
                context,
                "MMSE Reminder",
                "It’s time for the monthly MMSE test.",
                contentIntent
        );

        // Schedule next month's reminder upon firing
        MmseReminderScheduler.scheduleNextMonth(context);
    }
}


