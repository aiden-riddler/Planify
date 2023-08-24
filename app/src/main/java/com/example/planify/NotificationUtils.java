package com.example.planify;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationUtils {
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleNotification(Context context, String message, long notificationTimeMillis, int notificationId, String courseName) {

        Log.d("Planify", "Sheduling notification for: " + formatTime(notificationTimeMillis));
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("notificationID", notificationId);
        intent.putExtra("course", courseName);
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

    public static String formatTime(long timeInMillis) {
        // Format the time using SimpleDateFormat or any other method you prefer
        SimpleDateFormat sdf = new SimpleDateFormat("E, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private static int uniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    public static void cancelNotification(Context context, int notificationId) {
        Log.d("Planify", "Cancelling notification: " + notificationId);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create a PendingIntent for the NotificationReceiver with the same notificationId
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel the pendingIntent to remove the scheduled notification
        alarmManager.cancel(pendingIntent);
    }
}

