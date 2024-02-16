package com.appdev.alarmapp.AlarmManagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.snoozeAlarm
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
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SnoozeScreen(
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel,
    intent: Intent = Intent(),
    snoozeTrigger: () -> Unit = {},
    alarmEndHandle: () -> Unit = {},
) {
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val context = LocalContext.current
    val broadcastManager = LocalBroadcastManager.getInstance(context)
    val remainingTimeState = remember  { MutableStateFlow(0L) }

    val snoozeService by remember {
        mutableStateOf(SnoozeService())
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
    val dismissSettingsReceived: DismissSettings? by remember {
        mutableStateOf(intent.getParcelableExtra("dismissSet"))
    }

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
    val snoozeTimeRemaining by remember {
        mutableStateOf(alarmEntity?.timeInMillis?.let {
            minutesToMillis(alarmEntity!!.snoozeTime)
        })
    }

    LaunchedEffect(Unit) {
        alarmEntity?.let {
            val serviceIntent = Intent(context, SnoozeService::class.java).apply {
                putExtra("minutes", it.snoozeTime)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val remainingMillis = intent.getLongExtra("remainingMillis", 0)
                remainingTimeState.value = remainingMillis
            }
        }
        broadcastManager.registerReceiver(receiver, IntentFilter("countdown-tick"))

        onDispose {
            broadcastManager.unregisterReceiver(receiver)
        }
    }

    val remainingTime = remember { snoozeService.remainingTimeFlow }.collectAsState(initial = 0L)
    val remainingMinutes = remainingTime.value / (60 * 1000)
    val remainingSeconds = (remainingTime.value % (60 * 1000)) / 1000

    Log.d(
        "CHKSNO",
        "time we got: $remainingMinutes:${String.format("%02d", remainingSeconds)}"
    )

//    DisposableEffect(Unit) {
//        alarmEntity?.let {
//            mainViewModel.updateSnoozeTime(minutesToMillis(it.snoozeTime))
//        }
//        val timerServiceIntent = Intent(context, SnoozeService::class.java).apply {
//            putExtra("timeInMillis", snoozeTimeRemaining)
//            putExtra("alarmEntity", alarmEntity)
//            putExtra("dismissSettings", dismissSettingsReceived)
//            putExtra("notify", notifyIt)
//        }
////        context.startService(timerServiceIntent)
//
//        onDispose {
//            // Stop the service when composable is disposed
////            context.stopService(timerServiceIntent)
//        }
//    }

    val snoozeTime by mainViewModel.snoozeTime.collectAsState()
    Log.d("CHKSNO", "collected snooze time:  $snoozeTime}")

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
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
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
                    Text(
                        "$remainingMinutes:${String.format("%02d", remainingSeconds)}", fontSize = 50.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffb5c7ca), textAlign = TextAlign.Center
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                CustomButton(
                    onClick = {
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
                                Helper.stopStream()
                                textToSpeech.stop()
                                textToSpeech.shutdown()
                                alarmEndHandle()
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

fun minutesToLocalTime(minutes: Int): String {
    val mins = minutes / 60
    val secs = minutes % 60 // Calculate the remaining seconds
    return String.format("%02d:%02d", mins, secs)
}

fun minutesToMillis(minutes: Int): Long {
    return minutes * 60 * 1000L // Convert minutes to milliseconds
}

fun convertMillisToMinutesAndSeconds(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}