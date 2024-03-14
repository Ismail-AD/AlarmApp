package com.appdev.alarmapp.AlarmManagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.SnoozeTimer
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertMillisToHoursAndMinutes
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.getFormattedToday
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SnoozeScreen(
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel,
    intent: Intent = Intent(),
    onDismissCallback: DismissCallback,
    timerEndsCallback: TimerEndsCallback
) {

    val remainingTimeFlow = remember { MutableStateFlow(0L) }
    val context = LocalContext.current
    val utils by remember {
        mutableStateOf(Utils(context = context))
    }
    val closestSnoozeTimer by remember {
        mutableStateOf(utils.findClosestSnoozeTimer())
    }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var loading by remember { mutableStateOf(false) }
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }


    val systemUiController = rememberSystemUiController()
    BackHandler(false) {

    }
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )
    }
    val notificationService by remember { mutableStateOf(NotificationService(context)) }
    val alarmEntity: AlarmEntity? by remember {
        mutableStateOf(intent.getParcelableExtra("Alarm"))
    }
//    val dismissSettingsReceived: DismissSettings? by remember {
//        mutableStateOf(intent.getParcelableExtra("dismissSet"))
//    }

    val notifyIt = intent.getBooleanExtra("notify", false)

    if (notifyIt) {
        if (alarmEntity != null) {
            notificationService.showNotification(
                alarmEntity!!.localTime.toString() + " " + getAMPM(
                    alarmEntity!!.localTime
                )
            )
        } else {
            notificationService.cancelNotification()
        }
    }
    val alreadySnoozed by remember {
        mutableStateOf(utils.getSnoozeTimerById(alarmId = alarmEntity?.id ?: 0))
    }


    DisposableEffect(Unit) {
        Log.d("CHECKR","Alarm Object in Snooze Composable ${alarmEntity}")
        Helper.stopIncreasingVolume()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val idOfAlarmEntity = intent.getLongExtra("idOfAl", 0L)
                alarmEntity?.let {
                    if (idOfAlarmEntity == it.id) {
                        val remainingMillis = intent.getLongExtra("remainingMillis", 0L)
                        remainingTimeFlow.value = remainingMillis
                    }
                }

            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(
            receiver,
            IntentFilter("countdown-tick")
        )

        onDispose {
            Log.d("CHECKR","ON DISPOSE Alarm Object in Snooze Composable  ${alarmEntity}")
            if (remainingTimeFlow.value <= 0L && mainViewModel.isRealAlarm) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
                timerEndsCallback.onTimeEnds()
            }
            if (!mainViewModel.isRealAlarm) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
        }
    }


    LaunchedEffect(key1 = alreadySnoozed) {
        if (alreadySnoozed == null) {
            Log.d("CHKSM", "---------SERVICE STARTED FOR NEW ALARM")
            alarmEntity?.let {
                val serviceIntent = Intent(context, SnoozeService::class.java).apply {
                    Log.d("CHKSN", "collected snooze time: ${it.snoozeTime}}")
                    putExtra("minutes", it.snoozeTime)
                    putExtra("id", it.id)
                }
                val currentTimeMillis = System.currentTimeMillis()
                val finalTimeMillis = currentTimeMillis + (it.snoozeTime * 60000)
                val remainingTimeMillis = finalTimeMillis - System.currentTimeMillis()
                utils.startOrUpdateSnoozeTimer(SnoozeTimer(it.id, remainingTimeMillis))
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }


    val remainingTime by remainingTimeFlow.collectAsState()

    val remainingMinutes = remainingTime / (60 * 1000)
    val remainingSeconds = (remainingTime % (60 * 1000)) / 1000

    Log.d("CHKSN", "time we got: $remainingMinutes:${String.format("%02d", remainingSeconds)}")


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 35.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alarmEntity?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        getFormattedToday(), fontSize = 23.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffb5c7ca), textAlign = TextAlign.Center
                    )
                    Text(
                        convertMillisToHoursAndMinutes(System.currentTimeMillis()),
                        fontSize = 23.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffb5c7ca),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Column {
                    if (remainingTime == 0L) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            "$remainingMinutes:${String.format("%02d", remainingSeconds)}",
                            fontSize = 50.sp,
                            letterSpacing = 0.sp,
                            color = Color(0xffb5c7ca),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomButton(
                    onClick = {
                        alarmEntity?.id?.let {
                            utils.stopSnoozeTimer(it)
                        }
                        if (mainViewModel.isRealAlarm) {
                            if (mainViewModel.dummyMissionList.isNotEmpty()) {
                                val singleMission = mainViewModel.dummyMissionList.first()

                                mainViewModel.missionData(
                                    MissionDataHandler.AddCompleteMission(
                                        missionId = singleMission.missionID,
                                        repeat = singleMission.repeatTimes,
                                        repeatProgress = singleMission.repeatProgress,
                                        missionLevel = singleMission.missionLevel,
                                        missionName = singleMission.missionName,
                                        isSelected = singleMission.isSelected,
                                        setOfSentences = convertStringToSet(singleMission.selectedSentences),
                                        imageId =
                                        singleMission.imageId,
                                        codeId = singleMission.codeId
                                    )
                                )
                            }
                        }
                        when (mainViewModel.missionDetails.missionName) {
                            "Memory" -> {
                                controller.navigate(Routes.MissionScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Shake" -> {
                                controller.navigate(Routes.MissionShakeScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Math" -> {
                                controller.navigate(Routes.MissionMathScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Typing" -> {
                                controller.navigate(Routes.TypingPreviewScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Step" -> {
                                controller.navigate(Routes.StepDetectorScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Squat" -> {
                                controller.navigate(Routes.SquatMissionScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "Photo" -> {
                                controller.navigate(Routes.PhotoMissionPreviewScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            "QR/Barcode" -> {
                                controller.navigate(Routes.BarCodePreviewAlarmScreen.route) {
                                    popUpTo(Routes.PreviewAlarm.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            else -> {
                                Log.d(
                                    "CHKSM",
                                    "ALARM IS GOING TO END AS DISMISSED IS CLICKED............."
                                )
                                if(Utils(context).areSnoozeTimersEmpty()){
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Utils(context).getCurrentVolume(), 0)
                                    Utils(context).removeVolume()
                                }
                                Helper.stopStream()
                                vibrator.cancel()
                                textToSpeech.stop()
                                onDismissCallback.onDismissClicked()
                            }
                        }
                    },
                    text = if (mainViewModel.dummyMissionList.isNotEmpty()) "Start the mission" else "Dismiss",
                    height = 70.dp,
                    width = 0.9f, textColor = Color.White
                )
            }
        }
    }
}



