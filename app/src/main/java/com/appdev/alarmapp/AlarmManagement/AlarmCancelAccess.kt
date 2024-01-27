package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.AlarmCancelScreen
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionViewer.BarCodeMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.MathMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.MissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.PhotoMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.ShakeDetectionScreen
import com.appdev.alarmapp.ui.MissionViewer.SquatMission
import com.appdev.alarmapp.ui.MissionViewer.StepMission
import com.appdev.alarmapp.ui.MissionViewer.TypingMissionHandler
import com.appdev.alarmapp.ui.PreivewScreen.localTimeToMillis
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlarmCancelAccess : ComponentActivity() {
    val mainViewModel by viewModels<MainViewModel>()
    var dismissSettings: DismissSettings = DismissSettings()

    @Inject
    lateinit var textToSpeech: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmAppTheme {
                val alarmScheduler = AlarmScheduler(applicationContext, mainViewModel)
                val previewMode = intent.getBooleanExtra("Preview", false)

                if (!previewMode) {
                    mainViewModel.updateIsReal(true)
                    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                }
                mainViewModel.previewModeUpdate(true)
                mainViewModel.snoozeUpdate(false)
                val notify = intent.getBooleanExtra("notify", false)

                if (intent?.hasExtra("Alarm") == true) {
                    val receivedAlarm: AlarmEntity? = intent.getParcelableExtra("Alarm")
                    receivedAlarm?.let { alarm ->
                        mainViewModel.missionData(MissionDataHandler.AddList(missionsList = alarm.listOfMissions))

                        AlarmNavGraph(textToSpeech,
                            intent,
                            mainViewModel = mainViewModel,
                            snoozeTrigger = {
                                mainViewModel.updateIsReal(false)
                                mainViewModel.previewModeUpdate(false)
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }) {
                            if (mainViewModel.dummyMissionList.isEmpty()) {
                                if (alarm.isOneTime && !mainViewModel.hasSnoozed) {
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
                                    mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                                    mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                                    mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getTime(
                                            time = alarm.localTime
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getMilli(
                                            timeInMilli = alarm.timeInMillis
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.requestCode(
                                            reqCode = alarm.reqCode
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getMissions(
                                            missions = alarm.listOfMissions
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getSnoozeTime(
                                            getSnoozeTime = alarm.snoozeTime
                                        )
                                    )
                                    mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = false))
                                    mainViewModel.updateHandler(EventHandlerAlarm.update)
                                }
                                if (alarm.listOfDays.isNotEmpty() && mainViewModel.isRealAlarm) {
                                    val alarmEntity = AlarmEntity(
                                        id = alarm.id,
                                        snoozeTime = alarm.snoozeTime,
                                        timeInMillis = alarm.timeInMillis,
                                        snoozeTimeInMillis = 0,
                                        listOfMissions = alarm.listOfMissions,
                                        listOfDays = alarm.listOfDays,
                                        reqCode = (0..19992).random(),
                                        ringtone = alarm.ringtone,
                                        localTime = alarm.localTime,
                                        isGentleWakeUp = alarm.isGentleWakeUp,
                                        isTimeReminder = alarm.isTimeReminder,
                                        isLoudEffect = alarm.isLoudEffect,
                                        wakeUpTime = alarm.wakeUpTime,
                                        isLabel = alarm.isLabel,
                                        customVolume = alarm.customVolume,
                                        willVibrate = alarm.willVibrate,
                                        labelTextForSpeech = alarm.labelTextForSpeech,
                                    )
                                    scheduleTheAlarm(alarmEntity, alarmScheduler, notify)
                                }
                                mainViewModel.updateIsReal(false)
                                mainViewModel.previewModeUpdate(false)
                                if (alarm.isGentleWakeUp) {
                                    Helper.updateLow(false)
                                    Helper.stopIncreasingVolume()
                                }
                                Helper.updateCustomValue(100f)
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                }

//                if (intent?.hasExtra("ringtoneObj") == true) {
//                    val receivedRingtone: Ringtone? = intent.getParcelableExtra("ringtoneObj")
//                    receivedRingtone?.let { ringtone ->
//
//                        if (intent?.hasExtra("list") == true) {
//
//                            val receivedList =
//                                intent.getSerializableExtra("list") as? List<Missions>
//
//                            receivedList?.let { listOfMissions ->
//
//                                mainViewModel.missionData(MissionDataHandler.AddList(missionsList = listOfMissions))
//
//                                AlarmNavGraph(intent, mainViewModel = mainViewModel) {
//                                    if (mainViewModel.dummyMissionList.isEmpty()) {
//                                        if (intent.hasExtra("listOfDays")) {
//                                            val listOfDaysJson = intent.getStringExtra("listOfDays")
//
//                                            // Convert the JSON string back to a Set
//                                            val gson = Gson()
//                                            val type = object : TypeToken<Set<String>>() {}.type
//                                            val listOfDays =
//                                                gson.fromJson<Set<String>>(listOfDaysJson, type)
//                                            timeInM?.let { tIM ->
//                                                id?.let { ID ->
//                                                    val alarmEntity = AlarmEntity(
//                                                        id = ID.toLong(),
//                                                        snoozeTime = snoozeTime?.toInt() ?: 5,
//                                                        timeInMillis = tIM.toLong(),
//                                                        listOfMissions = listOfMissions,
//                                                        listOfDays = listOfDays,
//                                                        reqCode = (0..19992).random(),
//                                                        ringtone = ringtone,
//                                                        localTime = localTime
//                                                    )
//                                                    scheduleTheAlarm(
//                                                        alarmEntity,
//                                                        alarmScheduler,
//                                                        notify
//                                                    )
//                                                }
//                                            }
//                                        }
//                                        startActivity(Intent(this, MainActivity::class.java))
//                                        mainViewModel.updateIsReal(false)
//                                        finish()
//                                    }
//                                }
//                            }
//                        } else {
//                            AlarmNavGraph(intent, mainViewModel = mainViewModel) {
//                                if (mainViewModel.dummyMissionList.isEmpty()) {
//                                    if (intent.hasExtra("listOfDays")) {
//                                        val listOfDaysJson = intent.getStringExtra("listOfDays")
//
//                                        // Convert the JSON string back to a Set
//                                        val gson = Gson()
//                                        val type = object : TypeToken<Set<String>>() {}.type
//                                        val listOfDays =
//                                            gson.fromJson<Set<String>>(listOfDaysJson, type)
//                                        timeInM?.let { tIM ->
//                                            id?.let { ID ->
//                                                val alarmEntity = AlarmEntity(
//                                                    id = ID.toLong(),
//                                                    snoozeTime = snoozeTime?.toInt() ?: 5,
//                                                    timeInMillis = tIM.toLong(),
//                                                    listOfDays = listOfDays,
//                                                    reqCode = (0..19992).random(),
//                                                    ringtone = ringtone,
//                                                    localTime = localTime
//                                                )
//                                                scheduleTheAlarm(
//                                                    alarmEntity,
//                                                    alarmScheduler,
//                                                    notify
//                                                )
//                                            }
//                                        }
//                                    }
//                                    startActivity(Intent(this, MainActivity::class.java))
//                                    mainViewModel.updateIsReal(false)
//                                    finish()
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Handle the volume key events here
            // You can either do nothing to prevent the system from changing the volume
            // or perform a custom action on volume key press
            return true // Consume the event to prevent the system volume change
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        Helper.stopStream()
        super.onDestroy()
    }
}

fun scheduleTheAlarm(
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
    notify: Boolean,
) {
    val selectedTimeMillis = localTimeToMillis(alarmEntity.localTime)

    if (alarmEntity.listOfDays.isNotEmpty()) {
        val calendar = Calendar.getInstance()

        val nextOccurrence = alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .filter { it > calendar.get(Calendar.DAY_OF_WEEK) }
            .minOrNull() ?: alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .minOrNull()!!

        val daysUntilNextOccurrence = nextOccurrence - calendar.get(Calendar.DAY_OF_WEEK)

        if (daysUntilNextOccurrence < 0) {
            val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrence)
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())
        } else if (daysUntilNextOccurrence == 0) {
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
        } else {
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
        }

        alarmEntity.timeInMillis = calendar.timeInMillis

        val instant = Instant.ofEpochMilli(calendar.timeInMillis)
        val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
        alarmEntity.localTime = offsetTime.toLocalTime()
        alarmEntity.reqCode = (0..19992).random()

        alarmScheduler.schedule(alarmEntity, notify)

    }
}

private fun getDayOfWeek(day: String): Int {
    val daysOfWeek =
        listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    return daysOfWeek.indexOf(day.lowercase(Locale.ROOT)) + 1
}

@Composable
fun AlarmNavGraph(
    textToSpeech: TextToSpeech,
    intent: Intent,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel, snoozeTrigger: () -> Unit, alarmEnds: () -> Unit
) {
    NavHost(
        navController = controller,
        startDestination = Routes.PreviewAlarm.route,
    ) {
        composable(route = Routes.PreviewAlarm.route) {
            AlarmCancelScreen(textToSpeech, controller, mainViewModel, intent, snoozeTrigger) {
                alarmEnds()
            }
        }
        composable(route = Routes.MissionShakeScreen.route) {
            ShakeDetectionScreen(mainViewModel = mainViewModel, controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.BarCodePreviewAlarmScreen.route) {
            BarCodeMissionScreen(mainViewModel = mainViewModel, controller = controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.StepDetectorScreen.route) {
            StepMission(mainViewModel = mainViewModel, controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.SquatMissionScreen.route) {
            SquatMission(mainViewModel = mainViewModel, controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.PhotoMissionPreviewScreen.route) {
            PhotoMissionScreen(mainViewModel = mainViewModel, controller = controller) {
                alarmEnds()
            }
        }

        composable(route = Routes.TypingPreviewScreen.route) {
            TypingMissionHandler(mainViewModel = mainViewModel, controller = controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.MissionMathScreen.route) {
            MathMissionHandler(
                mainViewModel,
                missionLevel = mainViewModel.missionDetails.missionLevel,
                controller = controller
            ) {
                alarmEnds()
            }
        }
        composable(route = Routes.MissionScreen.route) {
            val sizeOfBlocks = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 3
                "Easy" -> 4
                "Normal" -> 5
                "Hard" -> 6
                else -> 3
            }
            val cubeHeightWidth = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 100.dp
                "Easy" -> 75.dp
                "Normal" -> 61.dp
                "Hard" -> 50.dp
                else -> 100.dp
            }
            val columnPadding = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 8.dp
                "Easy" -> 4.dp
                "Normal" -> 1.dp
                "Hard" -> 0.dp
                else -> 8.dp
            }
            val lazyRowHeight = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 100.dp
                "Easy" -> 80.dp
                "Normal" -> 65.dp
                "Hard" -> 55.dp
                else -> 100.dp
            }
            MissionHandlerScreen(
                cubeHeightWidth, columnPadding, lazyRowHeight,
                controller, totalSize = sizeOfBlocks,
                mainViewModel = mainViewModel
            ) {
                alarmEnds()
            }
        }
    }
}