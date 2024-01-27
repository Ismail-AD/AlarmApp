package com.appdev.alarmapp.ui.MissionViewer

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionDemos.BarCodeCameraPreview
import com.appdev.alarmapp.ui.MissionDemos.CameraPreview
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertStringToSet
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlin.math.min


@OptIn(ExperimentalGetImage::class)
@Composable
fun BarCodeMissionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController,
    alarmEndHandle: () -> Unit = {}
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    if (dismissSettings.muteTone) {
        Helper.stopStream()
    }
    var missionData by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(1f) }
    var isFlashOn by remember { mutableStateOf(false) }

    var openCamera by remember { mutableStateOf(false) }
    var isMatched by remember { mutableStateOf<Boolean?>(null) }
    val dataToBeMatched by remember {
        mutableStateOf(mainViewModel.selectedCode.qrCodeString)
    }

    val context = LocalContext.current

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value

    LaunchedEffect(key1 = mainViewModel.selectedCode){
        if(mainViewModel.missionDetails.codeId > 1){
            missionData = mainViewModel.selectedCode.qrCodeString
        }
    }

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
    LaunchedEffect(key1 = isMatched) {
        if (isMatched == false) {
            openCamera = false
            delay(2000)
            progress = 1f
            isMatched = null
        }
        if (isMatched == true) {
            openCamera = false
            delay(2000)
            mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = "",startProcess = false))
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
                            isSelected = singleMission.isSelected,
                            setOfSentences = convertStringToSet(singleMission.selectedSentences),
                            imageId = singleMission.imageId, codeId = singleMission.codeId
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
                                popUpTo(Routes.PreviewAlarm.route) {
                                    inclusive = true
                                }
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
                            Helper.stopStream()
                            alarmEndHandle()
                        }
                    }
                } else {
                    Helper.stopStream()
                    alarmEndHandle()
                }
            } else {
                controller.navigate(Routes.BarCodeDemoScreen.route) {
                    popUpTo(Routes.PreviewAlarm.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff121315)),
        contentAlignment = Alignment.TopCenter
    ) {

        when (isMatched) {
            false -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wrong),
                            contentDescription = "",
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "Code doesn't matched! Please try again...",
                            color = Color(0xffF44336),
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                            lineHeight = 35.sp
                        )

                    }
                }
            }

            true -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.angel),
                            contentDescription = "",
                            modifier = Modifier.size(95.dp)
                        )
                        Text(
                            text = "Have a nice day :)",
                            color = Color.White,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                            lineHeight = 35.sp
                        )

                    }
                }
            }

            else -> {
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

                    if (!openCamera) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
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
                                .fillMaxHeight(0.8f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Get ready to scan the QR/Barcode",
                                color = Color.White,
                                fontSize = 25.sp, textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W400,
                                modifier = Modifier.padding(horizontal = 80.dp), lineHeight = 35.sp
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                            Image(
                                painter = painterResource(id = R.drawable.barcode),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(150.dp)
                            )
                            Spacer(modifier = Modifier.height(35.dp))

                            if (mainViewModel.missionDetails.codeId > 1) {
                                mainViewModel.getCodeById(mainViewModel.missionDetails.codeId)
                                missionData?.let {
                                    Text(
                                        text = it,
                                        color = Color.White,
                                        fontSize = 18.sp, textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.W400,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = mainViewModel.selectedCode.qrCodeString,
                                    color = Color.White,
                                    fontSize = 18.sp, textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.W400,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(35.dp))
                            CustomButton(
                                onClick = {
                                    progress = 1f
                                    openCamera = true
                                    mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = "",startProcess = true))
                                },
                                text = "I'm ready",
                                width = 0.8f,
                                backgroundColor = Color(0xff7B70FF),
                                textColor = Color.White
                            )

                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backColor)
                        ) {
                            if (!isFlashOn) {
                                BarCodeCameraPreview(viewModel = mainViewModel) {
                                    isMatched =
                                        mainViewModel.detectedQrCodeState.qrCode == dataToBeMatched
                                    mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = "",startProcess = false))
                                }
                            } else {
                                BarCodeCameraPreview(viewModel = mainViewModel) {
                                    isMatched =
                                        mainViewModel.detectedQrCodeState.qrCode == dataToBeMatched
                                    mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = "",startProcess = false))
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopStart)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = {
                                    if (!mainViewModel.isRealAlarm) {
                                        Helper.playStream(context, R.raw.alarmsound)
                                    }
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
                                IconButton(
                                    onClick = {
                                        isFlashOn = !isFlashOn
                                        mainViewModel.updateFlash(isFlashOn)
                                    },
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                        contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                    }

                }
            }
        }

    }
}
