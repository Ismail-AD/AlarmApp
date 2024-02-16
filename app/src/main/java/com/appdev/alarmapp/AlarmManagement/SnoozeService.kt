package com.appdev.alarmapp.AlarmManagement

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
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
    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeInMillis = intent?.getLongExtra("timeInMillis", 0L)
        val alarmEntity = intent?.getParcelableExtra<AlarmEntity>("alarmEntity")
        val dismissSettingsReceived = intent?.getParcelableExtra<DismissSettings>("dismissSettings")
        val notifyIt = intent?.getBooleanExtra("notify", false)


        notifyIt?.let {
            if (it && alarmEntity != null) {
                val currentTimeMillis = System.currentTimeMillis()
                val finalTimeMillis = currentTimeMillis + (alarmEntity.snoozeTime * 60000)
                val finalTimeFormatted = convertMillisToFormattedTime(finalTimeMillis)

                notificationService.showNotification("Snooze until: $finalTimeFormatted")
            } else {
                notificationService.cancelNotification()
            }
        }
        timeInMillis?.let {
            val minutes = intent.getIntExtra("minutes", 0)
            val totalTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())

            countdownJob = CoroutineScope(Dispatchers.Default).launch {
                var remainingTimeMillis = totalTimeMillis
                while (remainingTimeMillis > 0) {
                    remainingTimeFlow.value = remainingTimeMillis
                    broadcastRemainingTime(remainingTimeMillis)
                    delay(1000) // Update every second
                    remainingTimeMillis -= 1000
                }
                val newIntent = Intent(applicationContext, AlarmCancelAccess::class.java)

                dismissSettingsReceived?.let { dismissSetting ->
                    newIntent.putExtra("dismissSet", dismissSetting)
                }
                alarmEntity?.let { alarm ->
                    newIntent.putExtra("Alarm", alarm)
                }
                notifyIt?.let {
                    newIntent.putExtra("notify", it)
                }
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(newIntent)

            }
        }

        return START_STICKY
    }
    fun convertMillisToFormattedTime(millis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun broadcastRemainingTime(remainingMillis: Long) {
        val intent = Intent("countdown-tick").apply {
            putExtra("remainingMillis", remainingMillis)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        countdownJob?.cancel()
        super.onDestroy()
    }
}