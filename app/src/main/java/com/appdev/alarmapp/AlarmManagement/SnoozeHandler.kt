package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.navigation.Routes
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
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeHandler : ComponentActivity(), DismissCallback, TimerEndsCallback {

    val mainViewModel by viewModels<MainViewModel>()
    var dismissSettings: DismissSettings? = null

    @Inject
    lateinit var textToSpeech: TextToSpeech
    @Inject
    lateinit var ringtoneRepository: RingtoneRepository
    private var receivedAlarm: AlarmEntity? = null
    private var previewMode: Boolean = false
    private var notify: Boolean = false
    private var timerEndsCalled: Boolean = false
    private var lastPressedKeyCode: Int = -1

    lateinit var alarm: AlarmEntity
    lateinit var alarmScheduler: AlarmScheduler
    lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CHKSM", "ON CREATE CALLED FOR SNOOZER")

        alarmScheduler = AlarmScheduler(applicationContext, ringtoneRepository)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!previewMode) {
            mainViewModel.updateIsReal(true)
        }

        Log.d("CHECKR", "${mainViewModel.isRealAlarm} real alarm state at snooze")
        setContent {
            AlarmAppTheme {
                Log.d("CHECKR", "In theme Composable of Snooze Handler")
                   mainViewModel.alarmIsSnoozed(true)
                   notify = intent.getBooleanExtra("notify", false)
                   if (intent?.hasExtra("Alarm") == true) {
                       receivedAlarm = intent.getParcelableExtra("Alarm")
                       dismissSettings = intent.getParcelableExtra("dismissSet")
                       receivedAlarm?.let { gotAlarm ->
                           alarm = gotAlarm
                           Log.d(
                               "CHECKR",
                               "Dummy List after addition ${mainViewModel.dummyMissionList.size}"
                           )
                           snoozeAlarmNavGraph(
                               onDismissCallback = this@SnoozeHandler,
                               timerEndsCallback = this@SnoozeHandler,
                               textToSpeech,
                               intent,
                               mainViewModel = mainViewModel,
                           )
                       }
               }
            }
        }
    }


    override fun onTimeEnds() {
//        Log.d("CHKMIS", "On timer ends triggerd ! value is ${Utils(this).getAlarmState(alarm.id.toString())}")
        val newIntent = Intent(this, AlarmCancelAccess::class.java)
        newIntent.putExtra("Alarm", alarm)
        newIntent.putExtra("notify", notify)
        newIntent.putExtra("dismissSet", dismissSettings)
        startActivity(newIntent)
        finish()
    }

//    override fun onSnoozeClicked() {
//        mainViewModel.updateIsReal(false)
//        Log.d(
//            "CHKSM",
//            "SNOOZE IS CALLED.............GOING TO FINISH ACTIVITY... and real state is ${mainViewModel.isRealAlarm}"
//        )
//        val newIntent = Intent(this, javaClass)
//        newIntent.putExtra("Alarm", receivedAlarm)
//        newIntent.putExtra("notify", notify)
//        newIntent.putExtra("dismissSet", dismissSettings)
//        startActivity(newIntent)
//        finish()
//    }

    override fun onStop() {
//        val isInvalidEnd = Utils(this).getAlarmState(alarm.id.toString())
        Log.d("CHKSM", "ON STOP TRIGGERED.............value is ${isFinishing}")

        if (!isFinishing && mainViewModel.dummyMissionList.isNotEmpty() && Utils(this).getSnoozeTimerById(
                alarm.id
            ) == null
        ) { // Power button not pressed, bring the app to the foreground
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
            Log.d("CHKSM", "CODE IN ON STOP TRIGGERED.............")

            val newIntent = Intent(this, AlarmCancelAccess::class.java)
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        lifecycleScope.launch {
            mainViewModel.basicSettings.collect{
                if(it.preventPhoneOff){
                    if (!hasFocus) {
                        val closeDialog = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                        sendBroadcast(closeDialog)
                    }
                }
            }
        }
    }

    override fun onDismissClicked() {
        alarmScheduler.cancel(alarm)
        mainViewModel.snoozeUpdate(false)

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
                Helper.stopIncreasingVolume()
                Helper.updateLow(false)
            }
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
            startActivity(Intent(this, EndingHandler::class.java))
            finish()
        }


    }
}


@Composable
fun snoozeAlarmNavGraph(
    onDismissCallback: DismissCallback,
    timerEndsCallback: TimerEndsCallback,
    textToSpeech: TextToSpeech,
    intent: Intent,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = controller,
        startDestination = Routes.SnoozeScr.route,
    ) {
        composable(route = Routes.SnoozeScr.route) {
            SnoozeScreen(
                textToSpeech,
                controller,
                mainViewModel,
                intent,
                onDismissCallback,
                timerEndsCallback
            )
        }
        composable(route = Routes.AlternativeMissionScreen.route) {
            AlterMissionScreen(
                intent, textToSpeech,
                dismissCallback =  onDismissCallback,
                timerEndsCallback = timerEndsCallback,
                controller = controller,
                mainViewModel = mainViewModel,
            )
        }
        composable(route = Routes.AtLocationMissionScreen.route) {
            AtLocationMission(
                textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                controller = controller,
                timerEndsCallback = timerEndsCallback,
                dismissCallback = onDismissCallback
            )
        }
        composable(route = Routes.WalkOffScreen.route) {
            WalkOffMission(
                textToSpeech = textToSpeech,
                mainViewModel = mainViewModel,
                controller = controller,
                timerEndsCallback = timerEndsCallback,
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
                timerEndsCallback = timerEndsCallback, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeShapesScreen.route) {
            ArrangeShapesMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback = timerEndsCallback, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeAlphabetsScreen.route) {
            ArrangeAlphabetsMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback = timerEndsCallback, dismissCallback = onDismissCallback
            )
        }
        composable(Routes.ArrangeNumbersScreen.route) {
            ArrangeNumbersMHScreen(
                textToSpeech = textToSpeech,
                controller = controller,
                mainViewModel = mainViewModel,
                timerEndsCallback = timerEndsCallback, dismissCallback = onDismissCallback
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
                timerEndsCallback = timerEndsCallback, dismissCallback = onDismissCallback
            )
        }


        composable(route = Routes.MissionShakeScreen.route) {
            ShakeDetectionScreen(
                intent, textToSpeech,
                mainViewModel = mainViewModel,
                controller,
                timerEndsCallback,
                onDismissCallback
            )
        }
        composable(route = Routes.BarCodePreviewAlarmScreen.route) {
            BarCodeMissionScreen(
                intent, textToSpeech,
                mainViewModel = mainViewModel,
                controller = controller, timerEndsCallback,
                onDismissCallback
            )
        }
        composable(route = Routes.StepDetectorScreen.route) {
            StepMission(
                intent, textToSpeech,
                mainViewModel = mainViewModel,
                controller,
                timerEndsCallback,
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
                controller = controller, timerEndsCallback,
                onDismissCallback
            )
        }

        composable(route = Routes.TypingPreviewScreen.route) {
            TypingMissionHandler(
                intent, textToSpeech,
                mainViewModel = mainViewModel,
                controller = controller, timerEndsCallback,
                onDismissCallback
            )
        }
        composable(route = Routes.MissionMathScreen.route) {
            MathMissionHandler(
                intent, textToSpeech,
                mainViewModel,
                missionLevel = mainViewModel.missionDetails.missionLevel,
                controller = controller,
                timerEndsCallback = timerEndsCallback,
                dismissCallback = onDismissCallback
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
                cubeHeightWidth,
                columnPadding,
                lazyRowHeight,
                controller,
                totalSize = sizeOfBlocks,
                mainViewModel = mainViewModel,
                timerEndsCallback = timerEndsCallback,
                dismissCallback = onDismissCallback
            )
        }
    }
}