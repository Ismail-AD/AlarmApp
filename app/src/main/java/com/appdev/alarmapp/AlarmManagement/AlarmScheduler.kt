package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.ringtoneList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(
    val context: Context,
    ringtoneRepository: RingtoneRepository
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val rp = ringtoneRepository

    fun schedule(alarmData: AlarmEntity, showNotify: Boolean) {
        val intent = Intent(context, AlarmTriggerHandler::class.java)
        intent.action = "${context.packageName}.ACTION_ALARM"
        intent.putExtra("notify", showNotify)
        CoroutineScope(Dispatchers.Main).launch{
            rp.getDismissSettings.collect{
                intent.putExtra("dismissSet", it)
            }
        }
        intent.putExtra("Alarm", alarmData)

        if (millisToMinutes(alarmData.snoozeTimeInMillis) != 0L) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmData.snoozeTimeInMillis,
                PendingIntent.getBroadcast(
                    context,
                    alarmData.id.toInt(),
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmData.timeInMillis,
                PendingIntent.getBroadcast(
                    context,
                    alarmData.id.toInt(),
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    fun millisToMinutes(millis: Long): Long {
        return millis / 60000
    }

    fun cancel(alarmData: AlarmEntity) {
        val intent = Intent(context, AlarmTriggerHandler::class.java)
        intent.action = "${context.packageName}.ACTION_ALARM"
        CoroutineScope(Dispatchers.Main).launch{
            rp.getBasicSettings.collect{
                intent.putExtra("notify",it.showInNotification)
            }
            rp.getDismissSettings.collect{
                intent.putExtra("dismissSet", it)
            }
        }
        intent.putExtra("Alarm", alarmData)
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                alarmData.id.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }


}