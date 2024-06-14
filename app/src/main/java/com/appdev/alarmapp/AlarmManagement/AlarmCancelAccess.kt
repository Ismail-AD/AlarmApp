package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
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
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.AlarmCancelScreen
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionViewer.AlterMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.ArrangeAlphabetsMHScreen
import com.appdev.alarmapp.ui.MissionViewer.ArrangeNumbersMHScreen
import com.appdev.alarmapp.ui.MissionViewer.ArrangeShapesMHScreen
import com.appdev.alarmapp.ui.MissionViewer.AtLocationMission
import com.appdev.alarmapp.ui.MissionViewer.BarCodeMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.MathMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.MissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.PhotoMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.RangedAlphabetsMissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.RangedNumbersMissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.ShakeDetectionScreen
import com.appdev.alarmapp.ui.MissionViewer.SquatMission
import com.appdev.alarmapp.ui.MissionViewer.StepMission
import com.appdev.alarmapp.ui.MissionViewer.TypingMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.WalkOffMission
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

    @Inject
    lateinit var ringtoneRepository: RingtoneRepository
    private var receivedAlarm: AlarmEntity? = null
    private var previewMode: Boolean = false
    private var notify: Boolean = false
    private var lastPressedKeyCode: Int = -1
    private var isScreenOnBeforeAlarm = false
    lateinit var alarm: AlarmEntity
    lateinit var alarmScheduler: AlarmScheduler
    lateinit var vibrator: Vibrator
    var volumeUpPressedTime: Long = 0
    var longPressDuration: Long = 1000L
    lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        isScreenOnBeforeAlarm = isScreenOn()
        alarmScheduler = AlarmScheduler(applicationContext, ringtoneRepository)

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
                            ringtoneRepository,
                            onDismissCallback = this@AlarmCancelAccess,
                            onSnoozeCallback = this@AlarmCancelAccess,
                            textToSpeech,
                            intent,
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }

    private val volumeUpLongPressRunnable = Runnable {
        if (System.currentTimeMillis() - volumeUpPressedTime >= longPressDuration) {

        }
    }


    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("CHKED", "Volume up/down captured")
            return true // Consume the event to prevent the system volume change
        }
        if (event?.keyCode == KeyEvent.KEYCODE_POWER) {
            Toast.makeText(getApplicationContext(), "Power dispatch", Toast.LENGTH_SHORT).show();
            lastPressedKeyCode = event.keyCode
            return true
        }
        return super.dispatchKeyEvent(event)
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (!hasFocus) {
//            val closeDialog = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            sendBroadcast(closeDialog)
//            Toast.makeText(this, "Your LongPress Power Button", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_POWER) {
            Toast.makeText(getApplicationContext(), "Power long press", Toast.LENGTH_SHORT).show();
            true
        } else super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_POWER) {
            Log.e("key", "long")
            Toast.makeText(
                applicationContext,
                "Power key donw",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onStop() {
        val isScreenOnNow = isScreenOn()
        if (((mainViewModel.missionDetailsList.isNotEmpty() && mainViewModel.dummyMissionList.isNotEmpty()) || mainViewModel.missionDetailsList.isEmpty()) && mainViewModel.isRealAlarm && KeyEvent.KEYCODE_POWER != lastPressedKeyCode && isScreenOnBeforeAlarm && isScreenOnNow
        ) { // Power button not pressed, bring the app to the foreground
            Log.d("CHKSM", "CODE IN ON STOP TRIGGERED.............")
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
            val newIntent = Intent(this, javaClass)
            newIntent.putExtra("Alarm", receivedAlarm)
            newIntent.putExtra("Preview", previewMode)
            newIntent.putExtra("notify", notify)
            newIntent.putExtra("dismissSet", dismissSettings)
            startActivity(newIntent)
            finish()
        } else if (((mainViewModel.missionDetailsList.isNotEmpty() && mainViewModel.dummyMissionList.isNotEmpty()) || mainViewModel.dummyMissionList.isEmpty()) && mainViewModel.isRealAlarm
        ) {
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
            val newIntent = Intent(this, javaClass)
            newIntent.putExtra("Alarm", receivedAlarm)
            newIntent.putExtra("Preview", previewMode)
            newIntent.putExtra("notify", notify)
            newIntent.putExtra("dismissSet", dismissSettings)
            startActivity(newIntent)
            finish()
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
        alarmScheduler.cancel(alarm)
        mainViewModel.snoozeUpdate(false)
        if (mainViewModel.dummyMissionList.isEmpty() && mainViewModel.isRealAlarm) {
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
                Helper.stopIncreasingVolume()
                Helper.updateLow(false)
            }
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
            startActivity(Intent(this, EndingHandler::class.java))
            Log.d(
                "CHKSM",
                "FINISHING ACTIVITY LASTLY ON CREATE.............and real state is ${mainViewModel.isRealAlarm}"
            )
            finish()
        } else if (previewMode) {
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
    ringtoneRepository: RingtoneRepository,
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
                intent, textToSpeech,
                onDismissCallback,
                onSnoozeCallback,
                controller,
                mainViewModel, ringtoneRepository
            )
        }
        composable(route = Routes.AlternativeMissionScreen.route) {
            AlterMissionScreen(
                intent, textToSpeech,
                dismissCallback = onDismissCallback,
                timerEndsCallback = object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                controller = controller,
                mainViewModel = mainViewModel,
            )
        }


        composable(route = Routes.AtLocationMissionScreen.route) {
            AtLocationMission(
                textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                controller = controller,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                dismissCallback = onDismissCallback
            )
        }
        composable(route = Routes.WalkOffScreen.route) {
            WalkOffMission(
                textToSpeech = textToSpeech,
                mainViewModel = mainViewModel,
                controller = controller,
                timerEndsCallback = object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                },
                dismissCallback = onDismissCallback
            )
        }
        composable(Routes.RangeAlphabetMissionPreview.route) {
            val diffLevel = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> mainViewModel.missionDetails.valuesToPick * 2
                "Hard Mode" -> mainViewModel.missionDetails.valuesToPick * 3
                else -> 0
            }

            val cubeHeightWidth =
                when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 60.dp
                    "Hard Mode" -> 50.dp
                    else -> 60.dp
                }
            val columnPadding = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> 7.dp
                "Hard Mode" -> 3.dp
                else -> 1.dp
            }
            val lazyRowHeight = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> 65.dp
                "Hard Mode" -> 55.dp
                else -> 65.dp
            }
            RangedAlphabetsMissionHandlerScreen(
                textToSpeech = textToSpeech,
                cubeHeightWidth = cubeHeightWidth,
                colPadding = columnPadding,
                rowHeight = lazyRowHeight,
                controller = controller,
                totalSize = diffLevel,
                mainViewModel = mainViewModel,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeShapesScreen.route) {
            ArrangeShapesMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeAlphabetsScreen.route) {
            ArrangeAlphabetsMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeNumbersScreen.route) {
            ArrangeNumbersMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.RangeMemoryMissionPreview.route) {
            val diffLevel = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> mainViewModel.missionDetails.valuesToPick * 2
                "Hard Mode" -> mainViewModel.missionDetails.valuesToPick * 3
                else -> 0
            }

            val cubeHeightWidth =
                when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 60.dp
                    "Hard Mode" -> 50.dp
                    else -> 60.dp
                }
            val columnPadding = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> 7.dp
                "Hard Mode" -> 3.dp
                else -> 1.dp
            }
            val lazyRowHeight = when (mainViewModel.missionDetails.difficultyLevel) {
                "Normal Mode" -> 65.dp
                "Hard Mode" -> 55.dp
                else -> 65.dp
            }
            RangedNumbersMissionHandlerScreen(
                textToSpeech = textToSpeech,
                cubeHeightWidth = cubeHeightWidth,
                colPadding = columnPadding,
                rowHeight = lazyRowHeight,
                controller = controller,
                totalSize = diffLevel,
                mainViewModel = mainViewModel,
                timerEndsCallback =
                object : TimerEndsCallback {
                    override fun onTimeEnds() {
                        TODO("Not yet implemented")
                    }

                }, dismissCallback = onDismissCallback
            )
        }




        composable(route = Routes.MissionShakeScreen.route) {
            ShakeDetectionScreen(
                intent, textToSpeech,
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
                intent, textToSpeech,
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
                intent, textToSpeech,
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
                intent, textToSpeech,
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
                intent, textToSpeech,
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
                intent, textToSpeech,
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
                intent, textToSpeech,
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