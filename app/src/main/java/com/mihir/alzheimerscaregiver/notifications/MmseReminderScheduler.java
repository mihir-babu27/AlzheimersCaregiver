package com.mihir.alzheimerscaregiver.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class MmseReminderScheduler {

    private static final int REQUEST_CODE = 2001;

    public static void scheduleMonthly(Context context) {
        scheduleAt(context, getNextTriggerTimeMillis());
    }

    public static void scheduleNextMonth(Context context) {
        scheduleAt(context, getNextMonthTimeMillis());
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = getPendingIntent(context);
        am.cancel(pi);
    }

    private static void scheduleAt(Context context, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = getPendingIntent(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MmseReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static long getNextTriggerTimeMillis() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1); // initial trigger soon; change to desired default time
        return c.getTimeInMillis();
    }

    private static long getNextMonthTimeMillis() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis();
    }
}


