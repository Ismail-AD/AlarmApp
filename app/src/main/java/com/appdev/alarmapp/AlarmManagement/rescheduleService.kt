package com.appdev.alarmapp.AlarmManagement

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.appdev.alarmapp.ModelClass.SnoozeTimer
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
        alarmScheduler = AlarmScheduler(applicationContext, ringtoneRepository)
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        deviceRestart = sharedPreferences.getBoolean("device_restarted", false)
        utils.clearSnoozeTimers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}