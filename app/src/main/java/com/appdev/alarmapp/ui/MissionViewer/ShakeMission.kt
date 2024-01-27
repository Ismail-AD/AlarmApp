package com.appdev.alarmapp.ui.MissionViewer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertStringToSet
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun ShakeDetectionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController,
    alarmEndHandle: () -> Unit = {}
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    if (dismissSettings.muteTone) {
        Helper.stopStream()
    }
    var progress by remember { mutableFloatStateOf(1f) }

    var shakeToBeDone by remember { mutableStateOf(mainViewModel.missionDetails.repeatTimes) }

    val context = LocalContext.current
    val sensorManager by remember {
        mutableStateOf(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
    }
    val accelerometerSensor by remember {
        mutableStateOf(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    }
    val shakeDetector by remember {
        mutableStateOf(ShakeDetector())
    }

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value

    LaunchedEffect(animatedProgress) {
        var elapsedTime = 0L
        val duration = dismissSettings.missionTime * 1000
        while (elapsedTime < duration && progress > 0.00100f) {
            val deltaTime = min(10, duration - elapsedTime)
            elapsedTime += deltaTime
            delay(deltaTime)
            progress -= deltaTime.toFloat() / duration // Decrement the progress
        }
    }
    LaunchedEffect(key1 = progress) {
        if (progress < 0.00100f) {
            Helper.playStream(context, R.raw.alarmsound)
            controller.navigate(Routes.PreviewAlarm.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }

    DisposableEffect(Unit) {

        val onShakeListener = object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                shakeToBeDone -= 1
            }

        }
        shakeDetector.setOnShakeListener(onShakeListener)

        sensorManager.registerListener(
            shakeDetector,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }
    LaunchedEffect(key1 = shakeToBeDone) {
        if (shakeToBeDone == 0) {
            if (mainViewModel.isRealAlarm) {
                val mutableList = mainViewModel.dummyMissionList.toMutableList()
                mutableList.removeFirst()
                mainViewModel.dummyMissionList = mutableList
                if (mainViewModel.dummyMissionList.isNotEmpty()) {
                    val singleMission = mainViewModel.dummyMissionList.first()

                    mainViewModel.missionData(
                        MissionDataHandler.AddCompleteMission(
                            missionId = singleMission.missionID,
                            repeat = singleMission.repeatTimes,
                            repeatProgress = singleMission.repeatProgress,
                            missionLevel = singleMission.missionLevel,
                            missionName = singleMission.missionName,
                            isSelected = singleMission.isSelected, setOfSentences = convertStringToSet(singleMission.selectedSentences), imageId = singleMission.imageId
                        , codeId = singleMission.codeId
                        )
                    )
                    when (mainViewModel.missionDetails.missionName) {
                        "Memory" -> {
                            controller.navigate(Routes.MissionScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "Shake" -> {
                            controller.navigate(Routes.MissionShakeScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "Math" -> {
                            controller.navigate(Routes.MissionMathScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "Typing" -> {
                            controller.navigate(Routes.TypingPreviewScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "Photo" -> {
                            controller.navigate(Routes.PhotoMissionPreviewScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
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
                        "Step" -> {
                            controller.navigate(Routes.StepDetectorScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "Squat" -> {
                            controller.navigate(Routes.SquatMissionScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        else -> {
                            alarmEndHandle()
                        }
                    }
                } else {
                    alarmEndHandle()
                }
            } else {
                controller.navigate(Routes.CommonMissionScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop
                }
            }
        } else {
            progress = 1f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff121315)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    trackColor = backColor,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp), progress = animatedProgress
                )

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    controller.navigate(Routes.PreviewAlarm.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIos,
                        contentDescription = "",
                        tint = Color.White, modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "Shake",
                    color = Color.White, fontSize = 23.sp,
                    fontWeight = FontWeight.W400,
                )
                Text(
                    text = "$shakeToBeDone",
                    color = Color.White,
                    fontSize = 70.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(top = 20.dp),
                    letterSpacing = 3.sp
                )
            }

        }
    }
}
