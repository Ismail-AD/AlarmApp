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
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.ringtoneList
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(
    val context: Context,
    mainViewModel: MainViewModel
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val mainVM = mainViewModel


    fun schedule(alarmData: AlarmEntity, showNotify: Boolean) {

        val intent = Intent(context, AlarmTriggerHandler::class.java)

        intent.putExtra("notify", showNotify)
        intent.putExtra("dismissSet", mainVM.dismissSettings.value)
        intent.putExtra("Alarm", alarmData)
        intent.action = "${context.packageName}.ACTION_ALARM"

        Log.d("CHKSM", "Scheduler on work")

        if (millisToMinutes(alarmData.snoozeTimeInMillis) != 0L) {
            Log.d("CHKNB", "IN SNOOZE SETTER")
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
        Log.d(
            "CHKZ",
            "In Cancel Method ....  Id is ${alarmData.id.toInt()}"
        )
        val intent = Intent(context, AlarmTriggerHandler::class.java)

        intent.putExtra("notify", mainVM.basicSettings.value.showInNotification)
        intent.putExtra("dismissSet", mainVM.dismissSettings.value)
        intent.putExtra("Alarm", alarmData)
        intent.action = "${context.packageName}.ACTION_ALARM"
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