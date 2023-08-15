package com.example.planify;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationUtils {
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleNotification(Context context, String message, long notificationTimeMillis, int notificationId) {

        Log.d("Planify", "Setting notification");
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("notificationID", notificationId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueNotificationId(), // Generate a unique notification ID
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Log.d("Planify", "Notification set");
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
            );
        }
    }

    private static int uniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    public static void cancelNotification(Context context, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create a PendingIntent for the NotificationReceiver with the same notificationId
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel the pendingIntent to remove the scheduled notification
        alarmManager.cancel(pendingIntent);
    }
}

