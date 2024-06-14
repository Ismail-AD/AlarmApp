package com.appdev.alarmapp.AlarmManagement

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.appdev.alarmapp.ModelClass.SnoozeTimer
import com.appdev.alarmapp.R
import com.appdev.alarmapp.Repository.AlarmRepository
import com.appdev.alarmapp.Repository.RingtoneRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class rescheduleService : Service() {
    lateinit var utils: Utils
    lateinit var sharedPreferences: SharedPreferences
    var deviceRestart: Boolean = false

    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var ringtoneRepository: RingtoneRepository

    @Inject
    lateinit var alarmRepository: AlarmRepository
    override fun onCreate() {
        super.onCreate()
        utils = Utils(applicationContext)
        alarmScheduler = AlarmScheduler(applicationContext, ringtoneRepository)
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        deviceRestart = sharedPreferences.getBoolean("device_restarted", false)
        utils.clearSnoozeTimers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        CoroutineScope(Dispatchers.Main).launch {
            if (deviceRestart) {
                val alarmList = alarmRepository.getAllAlarmsAsync()
                alarmList.forEach {
                    if (it.isActive && it.timeInMillis != it.nextTimeInMillis) {
                        ringtoneRepository.getBasicSettings.collect { alarmSet ->
                            it.timeInMillis = it.nextTimeInMillis
                            alarmScheduler.schedule(
                                it,
                                alarmSet.showInNotification
                            )
                        }
                    }
                    if (it.isActive) {
                        ringtoneRepository.getBasicSettings.collect { alarmSet ->
                            alarmScheduler.schedule(
                                it,
                                alarmSet.showInNotification
                            )
                        }
                    }
                }
                sharedPreferences.edit().putBoolean("device_restarted", false).apply()
                stopSelf(startId)
            }
        }
        return START_STICKY
    }
    private fun createNotificationChannel() {
        val channelId = "reschedule_service_channel"
        val channelName = "Reschedule Service Channel"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val channelId = "reschedule_service_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Reschedule Service")
            .setContentText("Rescheduling alarms.....")
            .setSmallIcon(R.drawable.alarmlogo).setOngoing(false).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return notificationBuilder.build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}