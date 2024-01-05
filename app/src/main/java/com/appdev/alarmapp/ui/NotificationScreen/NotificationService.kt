package com.appdev.alarmapp.ui.NotificationScreen

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.MainScreen.getAMPM
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

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java) // Replace with your activity
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
    fun cancelNotification() {
        notificationManager.cancel(1)
    }
}
