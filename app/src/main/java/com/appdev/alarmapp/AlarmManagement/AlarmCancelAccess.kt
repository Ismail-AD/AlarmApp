package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClass.SnoozeTimer
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
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlarmCancelAccess : ComponentActivity(), SnoozeCallback, DismissCallback {

    val mainViewModel by viewModels<MainViewModel>()
    var dismissSettings: DismissSettings? = null


    @Inject
    lateinit var textToSpeech: TextToSpeech
    private var receivedAlarm: AlarmEntity? = null
    private var previewMode: Boolean = false
    private var notify: Boolean = false
    private var lastPressedKeyCode: Int = -1
    private var isScreenOnBeforeAlarm = false
    lateinit var alarm: AlarmEntity
    lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CHKSM", "ON CREATE CALLED")

        isScreenOnBeforeAlarm = isScreenOn()
        alarmScheduler = AlarmScheduler(applicationContext, mainViewModel)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        previewMode = intent.getBooleanExtra("Preview", false)


        if (!previewMode) {
            Log.d("CHKMUS", "---ALARM STATE UPDATED REAL TO TRUE FROM MAIN ALARM---")

            mainViewModel.updateIsReal(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        }

        setContent {
            AlarmAppTheme {
                mainViewModel.previewModeUpdate(true)
                mainViewModel.snoozeUpdate(false)
                notify = intent.getBooleanExtra("notify", false)
                if (intent?.hasExtra("Alarm") == true) {
                    receivedAlarm = intent.getParcelableExtra("Alarm")
                    dismissSettings = intent.getParcelableExtra("dismissSet")
                    receivedAlarm?.let { gotAlarm ->
                        alarm = gotAlarm
                        mainViewModel.missionData(MissionDataHandler.AddList(missionsList = alarm.listOfMissions))
                        AlarmNavGraph(
                            onDismissCallback = this@AlarmCancelAccess,
                            onSnoozeCallback = this@AlarmCancelAccess,
                            textToSpeech,
                            intent,
                            mainViewModel = mainViewModel,
                        )
                    }
                }
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
        if (event?.keyCode == KeyEvent.KEYCODE_POWER) {
            // Store the last pressed key code
            lastPressedKeyCode = event.keyCode
        }
        return super.dispatchKeyEvent(event)
    }


    override fun onStop() {
        val isScreenOnNow = isScreenOn()
        Log.d("CHKSM", "ON STOP TRIGGERED.............${mainViewModel.getRealUpdate()}")

        if (mainViewModel.isRealAlarm && KeyEvent.KEYCODE_POWER != lastPressedKeyCode && isScreenOnBeforeAlarm && isScreenOnNow
        ) { // Power button not pressed, bring the app to the foreground
            Log.d("CHKSM", "CODE IN ON STOP TRIGGERED.............")

            val newIntent = Intent(this, javaClass)
            newIntent.putExtra("Alarm", receivedAlarm)
            newIntent.putExtra("Preview", previewMode)
            newIntent.putExtra("notify", notify)
            newIntent.putExtra("dismissSet", dismissSettings)
            startActivity(newIntent)
        }
        Log.d("CHKSM", "ON STOP CALLED.............")
        super.onStop()
    }

    private fun isScreenOn(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }


    override fun onSnoozeClicked() {
        mainViewModel.updateIsReal(false)
        if (Utils(this).areSnoozeTimersEmpty()) {
            Log.d(
                "CHKSM",
                "SNOOZE IS CALLED.............GOING TO FINISH ACTIVITY... and real state is ${mainViewModel.isRealAlarm}"
            )
            val newIntent = Intent(this, SnoozeHandler::class.java)
            newIntent.putExtra("Alarm", receivedAlarm)
            newIntent.putExtra("notify", notify)
            newIntent.putExtra("dismissSet", dismissSettings)
            startActivity(newIntent)
            finish()
        } else {
            val serviceIntent = Intent(this, SnoozeService::class.java).apply {
                Log.d("CHKN", "collected snooze time: ${alarm.snoozeTime}}")
                putExtra("minutes", alarm.snoozeTime)
                putExtra("id", alarm.id)
            }
            val currentTimeMillis = System.currentTimeMillis()
            val finalTimeMillis = currentTimeMillis + (alarm.snoozeTime * 60000)
            val remainingTimeMillis = finalTimeMillis - System.currentTimeMillis()
            Utils(this).startOrUpdateSnoozeTimer(SnoozeTimer(alarm.id, remainingTimeMillis))
            ContextCompat.startForegroundService(this, serviceIntent)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDismissClicked() {
        Log.d("CHKSM", "ON ALARM WORK FINISH IS CALLED.............")
        if (mainViewModel.dummyMissionList.isEmpty()) {
            if (alarm.isOneTime && !mainViewModel.hasSnoozed) {
                mainViewModel.updateHandler(
                    EventHandlerAlarm.Vibrator(
                        setVibration = alarm.willVibrate
                    )
                )
                mainViewModel.updateHandler(
                    EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime)
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
                    EventHandlerAlarm.getMissions(
                        missions = alarm.listOfMissions
                    )
                )
                mainViewModel.updateHandler(
                    EventHandlerAlarm.getSnoozeTime(
                        getSnoozeTime = alarm.snoozeTime
                    )
                )
                mainViewModel.updateHandler(
                    EventHandlerAlarm.CustomVolume(
                        customVolume = alarm.customVolume
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
                    ringtone = alarm.ringtone,
                    isActive = true,
                    localTime = alarm.localTime,
                    isGentleWakeUp = alarm.isGentleWakeUp,
                    isTimeReminder = alarm.isTimeReminder,
                    isLoudEffect = alarm.isLoudEffect,
                    wakeUpTime = alarm.wakeUpTime,
                    isLabel = alarm.isLabel,
                    customVolume = alarm.customVolume,
                    willVibrate = alarm.willVibrate,
                    labelTextForSpeech = alarm.labelTextForSpeech,
                    skipTheAlarm = false, isOneTime = false
                )
                mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarmEntity.id))
                mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarmEntity.listOfDays))
                mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarmEntity.ringtone))
                mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = alarmEntity.skipTheAlarm))
                mainViewModel.updateHandler(EventHandlerAlarm.LoudEffect(isLoudEffectOrNot = alarmEntity.isLoudEffect))
                mainViewModel.updateHandler(
                    EventHandlerAlarm.TimeReminder(
                        isTimeReminderOrNot = alarmEntity.isTimeReminder
                    )
                )
                mainViewModel.updateHandler(EventHandlerAlarm.IsGentleWakeUp(isGentleWakeUp = alarmEntity.isGentleWakeUp))
                mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = alarmEntity.wakeUpTime))
                mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = alarmEntity.willVibrate))
                mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = alarmEntity.customVolume))
                mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = alarmEntity.isLabel))
                mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = alarmEntity.labelTextForSpeech))
                mainViewModel.updateHandler(
                    EventHandlerAlarm.getTime(
                        time = alarmEntity.localTime
                    )
                )
                mainViewModel.updateHandler(
                    EventHandlerAlarm.isOneTime(isOneTime = alarmEntity.isOneTime)
                )
                mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = alarmEntity.listOfMissions))
                mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = alarmEntity.snoozeTime))
                mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarmEntity.isActive))
                mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarmEntity.timeInMillis))
                scheduleTheAlarm(alarmEntity, alarmScheduler, notify, true, mainViewModel)

            }
            mainViewModel.updateIsReal(false)
            if (alarm.isGentleWakeUp) {
                Helper.updateLow(false)
                Helper.stopIncreasingVolume()
            }
            Helper.updateCustomValue(100f)
            Helper.stopStream()
            startActivity(Intent(this, MainActivity::class.java))
            Log.d(
                "CHKSM",
                "FINISHING ACTIVITY LASTLY ON CREATE.............and real state is ${mainViewModel.isRealAlarm}"
            )
            finish()
        }


    }
}

fun scheduleTheAlarm(
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
    notify: Boolean,
    resetUpcoming: Boolean = false, mainViewModel: MainViewModel
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

        Log.d(
            "CHKALARM",
            "Calculated Time in millis to trigger: ${convertMillisToLocalTime(alarmEntity.timeInMillis)}"
        )

        val instant = Instant.ofEpochMilli(calendar.timeInMillis)
        val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
        alarmEntity.localTime = offsetTime.toLocalTime()

        Log.d("CHKZ", "Scheduling FROM Main screen with list of days ....  Id is ${alarmEntity.id}")
        alarmScheduler.schedule(alarmEntity, notify)
        if (resetUpcoming) {
            Log.d("CHKITO", "${calendar.timeInMillis} before new alarm set for another day")
            mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = calendar.timeInMillis))
            mainViewModel.updateHandler(EventHandlerAlarm.update)
        }


    } else if (alarmEntity.isOneTime) {
        Log.d("CHKZ", "Scheduling FROM Main screen with One time.... Id is ${alarmEntity.id}")

        if (alarmEntity.timeInMillis > System.currentTimeMillis()) {
            alarmScheduler.schedule(alarmEntity, notify)
        } else {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = alarmEntity.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1) // Add one day
            }
            alarmEntity.timeInMillis = calendar.timeInMillis
            alarmScheduler.schedule(alarmEntity, notify)
        }
    }
}

fun getDayOfWeek(day: String): Int {
    val daysOfWeek =
        listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    return daysOfWeek.indexOf(day.lowercase(Locale.ROOT)) + 1
}

@Composable
fun AlarmNavGraph(
    onDismissCallback: DismissCallback,
    onSnoozeCallback: SnoozeCallback,
    textToSpeech: TextToSpeech,
    intent: Intent,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = controller,
        startDestination = Routes.PreviewAlarm.route,
    ) {
        composable(route = Routes.PreviewAlarm.route) {
            AlarmCancelScreen(
                onDismissCallback,
                onSnoozeCallback,
                textToSpeech,
                controller,
                mainViewModel,
                intent
            )
        }

        composable(route = Routes.MissionShakeScreen.route) {
            ShakeDetectionScreen(
                mainViewModel = mainViewModel,
                controller, timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                onDismissCallback
            )
        }
        composable(route = Routes.BarCodePreviewAlarmScreen.route) {
            BarCodeMissionScreen(
                mainViewModel = mainViewModel,
                controller = controller, timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                onDismissCallback
            )
        }
        composable(route = Routes.StepDetectorScreen.route) {
            StepMission(
                mainViewModel = mainViewModel,
                controller, timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                onDismissCallback
            )
        }
        composable(route = Routes.SquatMissionScreen.route) {
            SquatMission(mainViewModel = mainViewModel, controller, onDismissCallback)
        }
        composable(route = Routes.PhotoMissionPreviewScreen.route) {
            PhotoMissionScreen(
                mainViewModel = mainViewModel,
                controller = controller, timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                onDismissCallback
            )
        }

        composable(route = Routes.TypingPreviewScreen.route) {
            TypingMissionHandler(
                mainViewModel = mainViewModel,
                controller = controller, timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                onDismissCallback
            )
        }
        composable(route = Routes.MissionMathScreen.route) {
            MathMissionHandler(
                mainViewModel,
                missionLevel = mainViewModel.missionDetails.missionLevel,
                controller = controller, timerEndsCallback = object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
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
                mainViewModel = mainViewModel, timerEndsCallback = object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }
    }
}