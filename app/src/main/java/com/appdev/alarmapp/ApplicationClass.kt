package com.appdev.alarmapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ui.PreivewScreen.RingViewModel
import com.appdev.alarmapp.utils.ObjectsGlobal
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApplicationClass: Application(){
    @Inject
    lateinit var tokenManagement: TokenManagement
    override fun onCreate() {
        super.onCreate()
        // A notification channel represents a category of notifications,
        // and each channel can have its own settings, such as sound, vibration, and importance level.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val mChannel = NotificationChannel(
                ObjectsGlobal.CHANNEL_ID,
                "Alarmy Notifications",
                NotificationManager.IMPORTANCE_LOW // Set importance to LOW for silent notifications
            )
            mChannel.description =
                "Current Channel is to notify about the set alarms."
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
    override fun onTerminate() {
        tokenManagement.removeDays()
        super.onTerminate()
    }
}