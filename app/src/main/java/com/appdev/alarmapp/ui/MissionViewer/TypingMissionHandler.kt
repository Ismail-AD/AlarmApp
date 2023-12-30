package com.appdev.alarmapp.ui.MissionViewer

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.MissionMathDemoHandler
import com.appdev.alarmapp.utils.convertStringToSet
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TypingMissionHandler(
    mainViewModel: MainViewModel, controller: NavHostController,
    alarmEndHandle: () -> Unit = {}
) {

    if (Helper.isPlaying()) {
        Helper.pauseStream()
    }

    var progress by remember { mutableFloatStateOf(1f) }
    var checkIt by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value

    var userInput by remember { mutableStateOf("") }
    var randomText by remember {
        mutableStateOf(
            if (mainViewModel.missionDetails.selectedSentences.isNotEmpty()) convertStringToSet(
                mainViewModel.missionDetails.selectedSentences
            ).random() else mainViewModel.getRandomSentence()
        )
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        keyboardController?.show()
        focusRequester.requestFocus()
    }

    LaunchedEffect(animatedProgress) {
        var elapsedTime = 0L
        val duration = 7500L // 3 seconds
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


    LaunchedEffect(
        key1 = userInput, key2 = checkIt
    ) {
        progress = 1f

        if (checkIt && userInput == randomText.phraseData && mainViewModel.missionDetails.repeatProgress == mainViewModel.missionDetails.repeatTimes) {
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
                            setOfSentences = convertStringToSet(singleMission.selectedSentences), imageId = singleMission.imageId
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

                        else -> {
                            alarmEndHandle()
                        }
                    }
                } else {
                    alarmEndHandle()
                }
            } else {
                controller.navigate(Routes.TypeMissionScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop
                }
            }
        }
        if (checkIt && userInput == randomText.phraseData && mainViewModel.missionDetails.repeatProgress != mainViewModel.missionDetails.repeatTimes) {
            delay(550)
            userInput = ""
            randomText = if (mainViewModel.missionDetails.selectedSentences.isNotEmpty()) convertStringToSet(
                mainViewModel.missionDetails.selectedSentences
            ).random() else mainViewModel.getRandomSentence()

            checkIt = false
            mainViewModel.missionData(MissionDataHandler.MissionProgress(mainViewModel.missionDetails.repeatProgress + 1))
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
                Text(
                    text = "${mainViewModel.missionDetails.repeatProgress} / ${mainViewModel.missionDetails.repeatTimes}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
            Column(
                modifier = Modifier.padding(
                    start = 15.dp, top = 15.dp, bottom = 15.dp, end = 2.dp
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = randomText.phraseData,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.W500, lineHeight = 35.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(start = 10.dp)
                    )
                    BasicTextField(
                        value = userInput,
                        onValueChange = {
                            if (it.length <= randomText.phraseData.length) {
                                userInput = it
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 27.sp,
                            fontWeight = FontWeight.W500,
                            color = when {
                                userInput == randomText.phraseData -> Color.Green.copy(alpha = 0.6f)  // Exact match
                                userInput.length <= randomText.phraseData.length &&
                                        userInput.indices.all { i -> userInput[i] == randomText.phraseData[i] } -> Color.White  // Index-by-index match
                                else -> Color.Blue.copy(alpha = 0.7f)  // No match
                            }, lineHeight = 35.sp, letterSpacing = (0.5).sp
                        ), cursorBrush = SolidColor(Color.White),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(250.dp)
                            .focusRequester(focusRequester),
                        maxLines = 8, decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    end = 2.dp,
                                    bottom = 20.dp
                                )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CustomButton(
                onClick = { checkIt = userInput == randomText.phraseData },
                text = "Complete",
                backgroundColor = signatureBlue,
                isEnabled = userInput == randomText.phraseData
            )
        }
    }
}