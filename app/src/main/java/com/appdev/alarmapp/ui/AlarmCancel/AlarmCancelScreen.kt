package com.appdev.alarmapp.ui.AlarmCancel

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.CustomImageButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertSetToString
import com.appdev.alarmapp.utils.convertStringToSet
import java.io.File
import java.time.Instant
import java.time.OffsetTime
import java.time.ZoneId

@Composable
fun AlarmCancelScreen(
    controller: NavHostController,
    mainViewModel: MainViewModel,
    intent: Intent = Intent(),
    alarmEndHandle: () -> Unit = {},
) {
    val context = LocalContext.current

    var alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, mainViewModel))
    }
    var ringtone by remember {
        mutableStateOf(Ringtone())
    }
    var showSnoozed by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = showSnoozed) {
        if (showSnoozed) {
            intent.getStringExtra("snooze")?.let { st ->
                Toast.makeText(context, "Alarm is snoozed for $st minutes", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    if (mainViewModel.isRealAlarm) {
        when (intent.getIntExtra("tonetype", 0)) {
            0 -> {
                val rawResourceId = intent.getStringExtra("ringtone")
                rawResourceId?.let {
                    ringtone = ringtone.copy(rawResourceId = it.toInt())
                    Helper.playStream(context, it.toInt())
                }
            }

            1 -> {
                val uriString = intent.getStringExtra("ringtone")
                val uri = Uri.parse(uriString)
                ringtone = ringtone.copy(uri = uri)
                Helper.playStream(context, uri = uri)
            }

            2 -> {
                val filePath = intent.getStringExtra("ringtone")
                filePath?.let {
                    val file = File(it)
                    ringtone = ringtone.copy(file = file)
                    Helper.playFile(file, context)
                }
            }

            else -> {}
        }
    } else {
        if (!Helper.isPlaying()) {
            Helper.playStream(context, R.raw.alarmsound)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            CustomButton(
                onClick = {
                    val snoozeTime = intent.getStringExtra("snooze")
                    val timeInM = intent.getStringExtra("tInM")
                    val id = intent.getStringExtra("id")

                    timeInM?.let { timeInMili ->
                        id?.let { ID ->
                            snoozeTime?.let { snooze ->
                                snoozeAlarm(
                                    AlarmEntity(
                                        id = ID.toLong(),
                                        timeInMillis = timeInMili.toLong(),
                                        snoozeTime = snooze.toInt(),
                                        ringtone = ringtone,
                                        reqCode = (0..19992).random()
                                    ), alarmScheduler
                                )
                                showSnoozed = true
                                Helper.stopStream()
                                alarmEndHandle()
                            }
                        }

                    }
                },
                text = "Snooze",
                backgroundColor = Color.White,
                textColor = Color.Black,
                width = 0.6f
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
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
                                        setOfSentences = convertStringToSet(singleMission.selectedSentences)
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
                            else -> {
                                Helper.stopStream()
                                alarmEndHandle()
                            }
                        }

                    },
                    text = if (mainViewModel.dummyMissionList.isNotEmpty()) "Start the mission" else "Dismiss",
                    height = 70.dp,
                    width = 0.9f
                )
            }
        }
        if (!mainViewModel.isRealAlarm) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                CustomImageButton(
                    onClick = {
                        Helper.stopStream()
                        controller.navigate(Routes.Preview.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    text = "Exit Preview Mode",
                    height = 60.dp,
                    width = 1.0f,
                    icon = Icons.Filled.Close,
                    iconColor = Color.Red,
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    roundedCornerShape = RoundedCornerShape(0.dp)
                )
            }
        }
    }
}

fun snoozeAlarm(alarmEntity: AlarmEntity, alarmScheduler: AlarmScheduler) {
    val snoozeMinutes =
        alarmEntity.snoozeTime // Set the snooze duration in minutes (adjust as needed)
    val currentTimeMillis = System.currentTimeMillis()

//    val instant1 = Instant.ofEpochMilli(alarmEntity.timeInMillis)
//    val offsetTime1 = OffsetTime.ofInstant(instant1, ZoneId.systemDefault())
//    Log.d("RINGC","LOCAL TIME BEFORE SNOOZE ADD ${offsetTime1.toLocalTime()}")

    val snoozeTimeMillis = currentTimeMillis + (snoozeMinutes * 60 * 1000)
    alarmEntity.timeInMillis = snoozeTimeMillis
    val instant = Instant.ofEpochMilli(snoozeTimeMillis)
    val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
    alarmEntity.localTime = offsetTime.toLocalTime()

//    Log.d("RINGC","LOCAL TIME AFTER SNOOZE ADD ${offsetTime.toLocalTime()}")

    // Reschedule the alarm with the updated time
    alarmScheduler.schedule(alarmEntity)
}