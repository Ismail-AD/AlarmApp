package com.appdev.alarmapp.ui.NotificationScreen

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.utils.ObjectsGlobal


class NotificationService(private val context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(text: String) {
        val notificationBuilder = NotificationCompat.Builder(context, ObjectsGlobal.CHANNEL_ID)
            .setSmallIcon(R.drawable.alarmlogo)
            .setContentTitle("Alarmy")
            .setContentText("[Next Alarm] $text")
            .setOngoing(true) // Make non-swipeable
            .setContentIntent(createPendingIntent()) // Add click action
            .build()
        notificationManager.notify(1, notificationBuilder)
    }


    fun showRestartNotification(text: String) {
        val notificationBuilder = NotificationCompat.Builder(context, ObjectsGlobal.CHANNEL_ID)
            .setSmallIcon(R.drawable.alarmlogo)
            .setContentTitle("Alarmy")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText(text)
            .build()
        notificationManager.notify(2, notificationBuilder)
    }
    fun generateRandomNotificationId(): Int {
        // Generate a random number for the notification ID
        return (0..Int.MAX_VALUE).random()
    }


    private fun createPendingIntent(): PendingIntent {
        // utils code to clear snooze data
        val intent = Intent(context, MainActivity::class.java) // Replace with your activity
        val requestID = System.currentTimeMillis().toInt()
        return PendingIntent.getActivity(context, requestID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    fun cancelNotification() {
        notificationManager.cancel(1)
    }
    fun cancelRestartNotification() {
        notificationManager.cancel(2)
    }
}
