package com.appdev.alarmapp.AlarmManagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import java.io.Serializable
import java.time.LocalTime

class AlarmTriggerHandler : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("CHKALM", "ON RECEIVE CALLED !")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // reset all alarms
//            context?.let {
//                AlarmScheduler(it, mainViewModel.recordingsList, mainViewModel.ringtoneSystemList).rescheduleAllAlarms()
//            }
        } else {

            val newIntent = Intent(context, AlarmCancelAccess::class.java)
            val ringtoneType = intent?.getIntExtra("tonetype", 0)
            val ringtoneData = intent?.getStringExtra("ringtone")
            val notify = intent?.getBooleanExtra("notify",false)

            if (intent?.hasExtra("dismissSet") == true) {
                val receivedSettings = intent.getParcelableExtra("dismissSet") as? DismissSettings
                receivedSettings?.let { dismissSetting ->
                    newIntent.putExtra("dismissSet",dismissSetting)
                }
            }

            if (intent?.hasExtra("list") == true) {
                val receivedList = intent.getSerializableExtra("list") as? List<Missions>
                receivedList?.let { listOfMissions ->
                    newIntent.putExtra("list", listOfMissions as Serializable)
                }
            }

            if (intent?.hasExtra("snooze") == true) {
                val snoozeTime = intent.getStringExtra("snooze")
                newIntent.putExtra("snooze", snoozeTime)
            }

            if (intent?.hasExtra("listOfDays") == true) {
                val listOfDaysJson = intent.getStringExtra("listOfDays")
                newIntent.putExtra("listOfDays", listOfDaysJson)
            }

            if (intent?.hasExtra("ringtoneObj") == true) {
                val receivedRingtone = intent.getParcelableExtra("ringtoneObj") as? Ringtone
                receivedRingtone?.let { ringtone ->
                    newIntent.putExtra("ringtoneObj", ringtone)
                }
            }

            val time = intent?.getStringExtra("tInM")
            val id = intent?.getStringExtra("id")
            val receivedLocalTimeString = intent?.getStringExtra("localTime")
            receivedLocalTimeString?.let { LTS->
                val receivedLocalTime = LocalTime.parse(LTS)
                newIntent.putExtra("localtime", receivedLocalTime)
            }

            newIntent.putExtra("tInM", time)
            newIntent.putExtra("id", id)
            newIntent.putExtra("tonetype", ringtoneType)
            newIntent.putExtra("ringtone", ringtoneData)
            newIntent.putExtra("notify",notify)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context?.startActivity(newIntent)
                Log.e("ALMCHK", "Activity started successfully")
            } catch (e: Exception) {
                Log.e("ALMCHK", "Error starting activity: $e")
            }
        }

    }
}