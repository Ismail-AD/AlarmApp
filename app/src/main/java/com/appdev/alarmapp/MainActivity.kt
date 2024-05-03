package com.appdev.alarmapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.AlarmManagement.SnoozeHandler
import com.appdev.alarmapp.AlarmManagement.Utils
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.navigation.navGraph
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManagement: TokenManagement

    @Inject
    lateinit var textToSpeech: TextToSpeech
    val mainViewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var ringtoneRepository: RingtoneRepository
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        val utils = Utils(this)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)


        lifecycleScope.launch {
            val closestSnoozeTimer = utils.findClosestSnoozeTimer()
            if (closestSnoozeTimer != null && !utils.areSnoozeTimersEmpty()
            ) {

                mainViewModel.alarmList.collect { list ->
                    val alarmToTrigger =
                        list.find { it.id == closestSnoozeTimer.alarmId }
                    alarmToTrigger?.let { alarm ->
                        if (utils.getSnoozeTimerById(alarm.id) != null) {
                            val newIntent =
                                Intent(this@MainActivity, SnoozeHandler::class.java)
                            mainViewModel.getMissionsById(alarm.id)
                            mainViewModel.missions.collect {
                                Log.d("CHKAI", "Missions ${it.listOfMissions}")
                                if (it.listOfMissions.isEmpty()) {
                                    newIntent.putExtra("Alarm", alarm)
                                    newIntent.putExtra(
                                        "notify",
                                        mainViewModel.basicSettings.value.showInNotification
                                    )
                                    newIntent.putExtra(
                                        "dismissSet",
                                        mainViewModel.dismissSettings.value
                                    )
                                    startActivity(newIntent)
                                    mainViewModel.updateMissions(
                                        missionsEntity(
                                            0,
                                            emptyList()
                                        )
                                    )
                                    finish()
                                } else {

                                    if (it.id == alarm.id) {
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.Vibrator(
                                                setVibration = alarm.willVibrate
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.CustomVolume(
                                                customVolume = alarm.customVolume
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.IsLabel(
                                                isLabelOrNot = alarm.isLabel
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.LabelText(
                                                getLabelText = alarm.labelTextForSpeech
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.LoudEffect(
                                                isLoudEffectOrNot = alarm.isLoudEffect
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.TimeReminder(
                                                isTimeReminderOrNot = alarm.isTimeReminder
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.IsGentleWakeUp(
                                                isGentleWakeUp = alarm.isGentleWakeUp
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.GetWakeUpTime(
                                                getWUTime = alarm.wakeUpTime
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.isActive(
                                                isactive = alarm.isActive
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.getDays(days = alarm.listOfDays)
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.ringtone(
                                                ringtone = alarm.ringtone
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.skipAlarm(
                                                skipped = alarm.skipTheAlarm
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime)
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.getTime(
                                                time = alarm.localTime
                                            )
                                        )
                                        mainViewModel.missionData(
                                            MissionDataHandler.AddList(
                                                missionsList = it.listOfMissions
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.idAlarm(iD = alarm.id)
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.isOneTime(
                                                isOneTime = alarm.isOneTime
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.getMilli(
                                                timeInMilli = alarm.timeInMillis
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.getNextMilli(
                                                upcomingMilli = alarm.nextTimeInMillis
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.getSnoozeTime(
                                                getSnoozeTime = alarm.snoozeTime
                                            )
                                        )
                                        mainViewModel.updateHandler(
                                            EventHandlerAlarm.update
                                        )
                                        if (alarm.listOfMissions.isEmpty()) {
                                            alarm.listOfMissions = it.listOfMissions
                                        }
                                        newIntent.putExtra("Alarm", alarm)
                                        newIntent.putExtra(
                                            "notify",
                                            mainViewModel.basicSettings.value.showInNotification
                                        )
                                        newIntent.putExtra(
                                            "dismissSet",
                                            mainViewModel.dismissSettings.value
                                        )
                                        startActivity(newIntent)
                                        mainViewModel.updateMissions(
                                            missionsEntity(
                                                0,
                                                emptyList()
                                            )
                                        )
                                        finish()
                                    }

                                }
                            }
                        }
                    }
                }
            } else {

                setContent {
                    AlarmAppTheme {
                        val controller = rememberNavController()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager: AlarmManager =
                                getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            val hasPermission: Boolean =
                                alarmManager.canScheduleExactAlarms()
                            if (hasPermission) {
                                navGraph(
                                    textToSpeech,
                                    controller,
                                    tokenManagement,
                                    applicationContext, ringtoneRepository
                                )
                            } else {
                                val intent = Intent().apply {
                                    action =
                                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                }
                                startActivity(intent)
                            }
                        } else {
                            navGraph(
                                textToSpeech,
                                controller,
                                tokenManagement,
                                applicationContext, ringtoneRepository
                            )
                        }
                    }
                }
            }
        }
    }

}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        Greeting("Android")
//    }
//}