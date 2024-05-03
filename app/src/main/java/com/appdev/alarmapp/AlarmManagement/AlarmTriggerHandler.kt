package com.appdev.alarmapp.AlarmManagement

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.appdev.alarmapp.ApplicationClass
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClass.SnoozeTimer
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.Serializable
import java.time.LocalTime
import javax.inject.Inject


class AlarmTriggerHandler : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val sharedPreferences = it.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("device_restarted", true).apply()
                val serviceIntent = Intent(it, rescheduleService::class.java)
                ContextCompat.startForegroundService(it, serviceIntent)
//                val notificationService = NotificationService(it)
//                notificationService.showRestartNotification("Tap to open the app and automatically reschedule alarms after device restart")
            }
        } else {


            val newIntent = Intent(context, AlarmCancelAccess::class.java)

            val notify = intent?.getBooleanExtra("notify", false)

            if (intent?.hasExtra("dismissSet") == true) {
                val receivedSettings = intent.getParcelableExtra("dismissSet") as? DismissSettings
                receivedSettings?.let { dismissSetting ->
                    newIntent.putExtra("dismissSet", dismissSetting)
                }
            }
            if (intent?.hasExtra("Alarm") == true) {
                val receivedAlarm = intent.getParcelableExtra("Alarm") as? AlarmEntity
                receivedAlarm?.let { alarm ->
                    newIntent.putExtra("Alarm", alarm)
                }
            }

            newIntent.putExtra("notify", notify)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                Log.d("CHKSM", "Launching alarm activity vai broadcast receiver.....")
                context?.startActivity(newIntent)
            } catch (e: Exception) {
                Log.e("ALMCHK", "Error starting activity: $e")
            }
        }

    }
}