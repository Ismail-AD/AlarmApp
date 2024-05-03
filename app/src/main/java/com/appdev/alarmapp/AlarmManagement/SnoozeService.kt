package com.appdev.alarmapp.AlarmManagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.utils.ObjectsGlobal
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.CHANNEL_ID
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.CHANNEL_ID_SNOOZE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SnoozeService : Service() {
    private lateinit var notificationService: NotificationService
    var remainingTimeFlow = MutableStateFlow(0L)
    private var countdownJob: Job? = null
    lateinit var utils: Utils
    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(applicationContext)
        utils = Utils(applicationContext)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = NotificationChannel(
            CHANNEL_ID_SNOOZE,
            "Alarmy Snooze notifications",
            NotificationManager.IMPORTANCE_LOW // Set importance to low to make it silent
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)


        val timeInMillis = intent?.getLongExtra("timeInMillis", 0L)
        val alarmEntity = intent?.getParcelableExtra<AlarmEntity>("alarmEntity")
        val notifyIt = intent?.getBooleanExtra("notify", false)


        timeInMillis?.let {
            val minutes = intent.getIntExtra("minutes", 0)
            val idOfAlarm = intent.getLongExtra("id", 0L)
            val currentTimeMillis = System.currentTimeMillis()
            val finalTimeMillis = currentTimeMillis + (minutes * 60000)
            val finalTimeFormatted = convertMillisToFormattedTime(finalTimeMillis)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_SNOOZE)
                .setSmallIcon(R.drawable.alarmlogo)
                .setContentTitle("Alarmy")
                .setPriority(NotificationCompat.PRIORITY_LOW) // Set priority to low to make it silent
                .setContentText("Snooze until: $finalTimeFormatted")
                .build()


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(startId, notification, FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
            } else {
                startForeground(startId, notification)
            }
            val totalTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())


            countdownJob = CoroutineScope(Dispatchers.Default).launch {
                var remainingTimeMillis = totalTimeMillis
                while (remainingTimeMillis > 0 && utils.getSnoozeTimerById(idOfAlarm) != null) {
                    remainingTimeFlow.value = remainingTimeMillis
                    broadcastRemainingTime(remainingTimeMillis, idOfAlarm)
                    remainingTimeMillis = finalTimeMillis - System.currentTimeMillis()
                }
                stopForeground(true)
                utils.stopSnoozeTimer(idOfAlarm)
                stopSelf(startId)
            }
        }
        return START_STICKY
    }

    fun convertMillisToFormattedTime(millis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun broadcastRemainingTime(remainingMillis: Long, id: Long) {
        val intent = Intent("countdown-tick").apply {
            putExtra("remainingMillis", remainingMillis)
            putExtra("idOfAl", id)
        }
//        Log.d("CHKSN", "3. Going to broadcast remaining")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    override fun onDestroy() {
        countdownJob?.cancel()
        super.onDestroy()
    }
}