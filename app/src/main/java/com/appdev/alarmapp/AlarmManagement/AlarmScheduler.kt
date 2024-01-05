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
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.ringtoneList
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.io.Serializable
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(
    val context: Context,
    mainViewModel: MainViewModel
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val mainVM = mainViewModel
//    fun reshedul() {
    // Replace this with your logic to retrieve and reschedule alarms
//        val alarmList =
    // Fetch your alarm data (e.g., from a database)

//            for (alarmData in alarmList) {
//            schedule(alarmData)
//            }
//    }

    @SuppressLint("MissingPermission")
    fun schedule(alarmData: AlarmEntity,showNotify:Boolean) {

        val intent = Intent(context, AlarmTriggerHandler::class.java)


        if (alarmData.ringtone.rawResourceId != -1) {
            intent.putExtra("tonetype", 0)
            intent.putExtra("ringtone", alarmData.ringtone.rawResourceId.toString())
        } else if (alarmData.ringtone.uri != null) {
            intent.putExtra("tonetype", 1)
            intent.putExtra("ringtone", alarmData.ringtone.uri.toString())
        } else if (alarmData.ringtone.file != null) {
            intent.putExtra("tonetype", 2)
            intent.putExtra("ringtone", alarmData.ringtone.file!!.absolutePath)
        }
        if (alarmData.listOfMissions.isNotEmpty()) {
            intent.putExtra("list", alarmData.listOfMissions as Serializable)
        }
        if (alarmData.listOfDays.isNotEmpty()) {
            intent.putExtra("listOfDays", Gson().toJson(alarmData.listOfDays))
        }
        if (alarmData.snoozeTime != -1) {
            intent.putExtra("snooze", alarmData.snoozeTime.toString())
        }
        intent.putExtra("ringtoneObj", alarmData.ringtone)
        intent.putExtra("tInM", alarmData.timeInMillis.toString())
        intent.putExtra("id", alarmData.id.toString())
        intent.putExtra("localTime", alarmData.localTime.toString())
        intent.putExtra("notify", showNotify)
        intent.putExtra("dismissSet", mainVM.dismissSettings.value)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmData.timeInMillis,
            PendingIntent.getBroadcast(
                context,
                alarmData.reqCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun cancel(alarmData: AlarmEntity) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                alarmData.reqCode,
                Intent(context, AlarmTriggerHandler::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}