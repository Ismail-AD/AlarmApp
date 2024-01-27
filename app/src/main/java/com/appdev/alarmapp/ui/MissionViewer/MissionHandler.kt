package com.appdev.alarmapp.ui.MissionViewer

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.MissionDemoHandler
import com.appdev.alarmapp.utils.convertStringToSet
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min


@Composable
fun MissionHandlerScreen(
    cubeHeightWidth: Dp,
    colPadding: Dp,
    rowHeight: Dp,
    controller: NavHostController,
    totalSize: Int,
    missionViewModel: MissionViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    alarmEndHandle: () -> Unit = {}
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    if (dismissSettings.muteTone) {
        Helper.stopStream()
    }
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(1f) }
    var countdown by remember { mutableStateOf(3) }
    var showWrong by remember { mutableStateOf(missionViewModel.missionHandler.notMatched) }
    var modifiedIndices by remember {
        mutableStateOf(
            if (missionViewModel.missionHandler.preservedIndexes.isEmpty()) {
                missionViewModel.missionEventHandler(MissionDemoHandler.GenerateAndStore(totalSize))
                missionViewModel.missionHandler.preservedIndexes
            } else {
                missionViewModel.missionEventHandler(MissionDemoHandler.ResetData)
                missionViewModel.missionEventHandler(MissionDemoHandler.GenerateAndStore(totalSize))
                missionViewModel.missionHandler.preservedIndexes
            }
        )
    }


    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value
    val coroutineScope = rememberCoroutineScope()


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
    LaunchedEffect(key1 = countdown) {
        if (countdown > 0) {
            coroutineScope.launch {
                delay(1000)
                countdown--
            }
        }
    }
    LaunchedEffect(key1 = showWrong, key2 = missionViewModel.missionHandler.notMatched) {
        if (missionViewModel.missionHandler.notMatched) {
            delay(500)
            missionViewModel.missionEventHandler(MissionDemoHandler.updateMatch(false))
            showWrong = false
        }
    }
    LaunchedEffect(
        key1 = modifiedIndices,
        key2 = countdown,
        key3 = missionViewModel.missionHandler.correctChoiceList
    ) {
        if (missionViewModel.missionHandler.preservedIndexes.isEmpty() && countdown != 0) {
            missionViewModel.missionEventHandler(MissionDemoHandler.GenerateAndStore(totalSize))
            modifiedIndices = missionViewModel.missionHandler.preservedIndexes
        }
        if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && countdown == 0) {
            modifiedIndices = missionViewModel.missionHandler.correctChoiceList
        }

        if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && (missionViewModel.missionHandler.correctChoiceList.size == missionViewModel.missionHandler.preservedIndexes.size)) {
            delay(500)
        }

        if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && (missionViewModel.missionHandler.correctChoiceList.size == missionViewModel.missionHandler.preservedIndexes.size) && mainViewModel.missionDetails.repeatProgress == mainViewModel.missionDetails.repeatTimes) {
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
                            imageId = singleMission.imageId,
                            codeId = singleMission.codeId
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

                        "QR/Barcode" -> {
                            controller.navigate(Routes.BarCodePreviewAlarmScreen.route) {
                                popUpTo(Routes.PreviewAlarm.route) {
                                    inclusive = true
                                }
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
        }
        if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && (missionViewModel.missionHandler.correctChoiceList.size == missionViewModel.missionHandler.preservedIndexes.size) && mainViewModel.missionDetails.repeatProgress != mainViewModel.missionDetails.repeatTimes) {
            missionViewModel.missionEventHandler(MissionDemoHandler.ResetData)
            countdown = 3
            mainViewModel.missionData(MissionDataHandler.MissionProgress(mainViewModel.missionDetails.repeatProgress + 1))

        }

    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (countdown != 0) Color(0xff232E4C) else Color(0xff121315)),
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
                Text(
                    text = "${mainViewModel.missionDetails.repeatProgress} / ${mainViewModel.missionDetails.repeatTimes}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            Text(
                text = if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && (missionViewModel.missionHandler.correctChoiceList.size == missionViewModel.missionHandler.preservedIndexes.size)) "Round ${mainViewModel.missionDetails.repeatProgress} Cleared" else if (showWrong) "Wrong" else if (missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && countdown != 0) "Memorize!" + if (countdown > 0) " $countdown" else " " else "Spot ${missionViewModel.missionHandler.preservedIndexes.size - missionViewModel.missionHandler.correctChoiceList.size} color tiles",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(colPadding)
                ) {
                    items(totalSize) { row ->
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            items(totalSize) { column ->
                                RubikCubeBlock(
                                    modifier = Modifier
                                        .clipToBounds()
                                        .background(
                                            if ((missionViewModel.missionHandler.notMatched) && row * totalSize + column == missionViewModel.missionHandler.clicked) {
                                                showWrong = true
                                                Color.Red
                                            } else if (modifiedIndices.contains(
                                                    row * totalSize + column
                                                )
                                            ) Color(
                                                0xFF9BA2B2
                                            ) else Color(0xff1C1F26)
                                        )
                                        .clickable {
                                            if (countdown == 0) {
                                                // Handle block click during countdown
                                                missionViewModel.missionEventHandler(
                                                    MissionDemoHandler.checkMatch(row * totalSize + column)
                                                )

                                                progress = 1f
                                            }
                                        }, cubeHeightWidth
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RubikCubeBlock(modifier: Modifier = Modifier, cubeHeightWidth: Dp) {
    Box(
        modifier = modifier
            .height(cubeHeightWidth)
            .width(cubeHeightWidth),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.White
        )
    }
}

