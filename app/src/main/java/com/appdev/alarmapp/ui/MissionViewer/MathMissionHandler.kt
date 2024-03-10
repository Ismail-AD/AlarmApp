package com.appdev.alarmapp.ui.MissionViewer

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.DismissCallback
import com.appdev.alarmapp.AlarmManagement.TimerEndsCallback
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.MissionMathDemoHandler
import com.appdev.alarmapp.utils.convertStringToSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMissionHandler(
    mainViewModel: MainViewModel,
    missionLevel: String = "Very Easy",
    controller: NavHostController,
    missionViewModel: MissionViewModel = hiltViewModel(), timerEndsCallback: TimerEndsCallback, dismissCallback: DismissCallback
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()


    var progress by remember { mutableFloatStateOf(1f) }
    var countdown by remember { mutableStateOf(3) }
    var showWrong by remember { mutableStateOf(missionViewModel.missionMathHandler.notMatched) }
    var rightAnswer by remember { mutableStateOf(missionViewModel.missionMathHandler.answerCorrect) }
    var qusetionString by remember {
        mutableStateOf(getMathValues(missionLevel, missionViewModel))
    }
    var answer by remember { mutableStateOf("") }
    val context = LocalContext.current

    val keypadValues = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("✗", "0", "✓"),
    )

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit){
        if (dismissSettings.muteTone) {
            Helper.stopStream()
        }
    }
    BackHandler {

    }
    LaunchedEffect(key1 = showWrong, key2 = rightAnswer) {
        if (missionViewModel.missionMathHandler.answer == 0 && countdown != 0) {
            qusetionString = getMathValues(missionLevel, missionViewModel)
        }
        if (missionViewModel.missionMathHandler.notMatched) {
            delay(750)
            answer = ""
            missionViewModel.missionMathEventHandler(MissionMathDemoHandler.UpdateMatch(false))
            showWrong = false
        }
        if (mainViewModel.missionDetails.repeatProgress == mainViewModel.missionDetails.repeatTimes && missionViewModel.missionMathHandler.answerCorrect) {
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
                            isSelected = singleMission.isSelected, setOfSentences = convertStringToSet(singleMission.selectedSentences), imageId = singleMission.imageId, codeId = singleMission.codeId
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
                            dismissCallback.onDismissClicked()
                        }
                    }
                } else {
                    dismissCallback.onDismissClicked()
                }
            } else {
                controller.navigate(Routes.CommonMissionScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop
                }
            }
        }
        if (missionViewModel.missionMathHandler.answerCorrect && mainViewModel.missionDetails.repeatProgress != mainViewModel.missionDetails.repeatTimes) {
            delay(550)
            missionViewModel.missionMathEventHandler(MissionMathDemoHandler.ResetData)
            rightAnswer = false
            countdown = 3
            answer = ""
            mainViewModel.missionData(MissionDataHandler.MissionProgress(mainViewModel.missionDetails.repeatProgress + 1))
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
            if(!mainViewModel.isRealAlarm){
                controller.popBackStack()
            } else{
                if(!mainViewModel.isSnoozed){
                    controller.navigate(Routes.PreviewAlarm.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else{
                    timerEndsCallback.onTimeEnds()
                }
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
                    if(!mainViewModel.isRealAlarm){
                        controller.popBackStack()
                    } else{
                        if(!mainViewModel.isSnoozed){
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        } else{
                            timerEndsCallback.onTimeEnds()
                        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, bottom = 10.dp, top = 50.dp)
            ) {
                Text(
                    text = qusetionString,
                    color = Color.White,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(82.dp)
                            .width(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (missionViewModel.missionMathHandler.notMatched) {
                                showWrong = true
                                Color.Red
                            } else if (missionViewModel.missionMathHandler.answerCorrect) {
                                rightAnswer = true
                                Color(0xff58B25A)
                            } else Color.Transparent,
                            disabledContainerColor = if (missionViewModel.missionMathHandler.notMatched) {
                                showWrong = true
                                Color.Red
                            } else if (missionViewModel.missionMathHandler.answerCorrect) {
                                rightAnswer = true
                                Color(0xff58B25A)
                            } else Color.Transparent
                        ), enabled = false,
                        border = BorderStroke(
                            width = (1.5).dp,
                            color = if (missionViewModel.missionMathHandler.notMatched) {
                                showWrong = true
                                Color.Red
                            } else if (missionViewModel.missionMathHandler.answerCorrect) {
                                rightAnswer = true
                                Color(0xff58B25A)
                            } else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 12.dp, top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = if (answer.length > 6) {
                                    answer =
                                        StringBuilder(answer).delete(6, answer.length).toString()
                                    answer
                                } else answer,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.W600,
                                maxLines = 1,
                            )
                            if (missionViewModel.missionMathHandler.notMatched) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 25.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "",
                                        tint = Color.White,
                                        modifier = Modifier.size(35.dp)
                                    )
                                }
                            }
                            if (missionViewModel.missionMathHandler.answerCorrect) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 25.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "",
                                        tint = Color.White,
                                        modifier = Modifier.size(35.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .background(Color.Transparent)
                ) {
                    items(keypadValues) { rowValues ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            rowValues.forEach { value ->
                                Button(
                                    onClick = {
                                        when (value) {
                                            "✗" -> {
                                                if (answer.isNotEmpty()) {
                                                    // Remove the last digit
                                                    answer = answer.dropLast(1)
                                                }
                                            }

                                            "✓" -> {
                                                // Handle the submit action
                                                // For now, just print the answer
                                                if (answer.trim().isNotEmpty()) {
                                                    missionViewModel.missionMathEventHandler(
                                                        MissionMathDemoHandler.CheckMatch(
                                                            answer.trim().toInt()
                                                        )
                                                    )
                                                }
                                            }

                                            else -> {
                                                // Append the digit to the answer
                                                answer += value
                                            }
                                        }
                                        progress = 1f
                                    },
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .fillMaxHeight()
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (value == "✗") Color(0xff3F434F) else if (value == "✓") Color(
                                            0xffEB2641
                                        ) else Color(0xff24272E)
                                    )
                                ) {
                                    Text(
                                        text = value,
                                        color = Color.White, fontSize = 24.sp,
                                        fontWeight = FontWeight.W500,
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


//@Composable
//fun NoKeyboardTextField(
//    modifier: Modifier,
//    text: String,
//    textColor: Int
//) {
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            AppCompatEditText(context).apply {
//                isFocusable = true
//                isFocusableInTouchMode = true
//                showSoftInputOnFocus = false
//            }
//        },
//        update = { view ->
//            view.setTextColor(textColor)
//            view.setText(text)
//            view.setSelection(text.length)
//        }
//    )
//}

fun getMathValues(value:String, missionViewModel: MissionViewModel): String {
    val num1: Int
    val num2: Int
    val num3: Int

    when (value) {
        "Very Easy" -> {
            num1 = generateRandomNumber(1, 9)
            num2 = generateRandomNumber(1, 9)
            missionViewModel.missionMathEventHandler(
                MissionMathDemoHandler.SubmitAnswer(
                    performCalculation(value, num1, num2, num3 = 0)
                )
            )
            return "$num1 + $num2 = "
        }

        "Easy" -> {
            num1 = generateRandomNumber(10, 99)
            num2 = generateRandomNumber(10, 99)
            missionViewModel.missionMathEventHandler(
                MissionMathDemoHandler.SubmitAnswer(
                    performCalculation(value, num1, num2, num3 = 0)
                )
            )
            return "$num1 + $num2 = "
        }

        "Normal" -> {
            num1 = generateRandomNumber(10, 99)
            num2 = generateRandomNumber(10, 99)
            num3 = generateRandomNumber(10, 99)
            missionViewModel.missionMathEventHandler(
                MissionMathDemoHandler.SubmitAnswer(
                    performCalculation(value, num1, num2, num3)
                )
            )
            return "$num1 + $num2 + $num3 = "
        }

        "Hard" -> {
            num1 = generateRandomNumber(1, 15)
            num2 = generateRandomNumber(1, 15)
            num3 = generateRandomNumber(10, 99)
            missionViewModel.missionMathEventHandler(
                MissionMathDemoHandler.SubmitAnswer(
                    performCalculation(value, num1, num2, num3)
                )
            )
            return "($num1 x $num2) + $num3 = "
        }

        else -> {
            num1 = generateRandomNumber(1, 9)
            num2 = generateRandomNumber(1, 9)
            missionViewModel.missionMathEventHandler(
                MissionMathDemoHandler.SubmitAnswer(
                    performCalculation(value, num1, num2, num3 = 0)
                )
            )
            return "$num1 + $num2 = "
        }
    }
}

fun performCalculation(value: String, num1: Int, num2: Int, num3: Int): Int {
    return when (value) {
        "Very Easy" -> (num1 + num2)
        "Easy" -> (num1 + num2)
        "Normal" -> (num1 + num2 + num3)
        "Hard" -> (num1 * num2) + (num3)
        else -> {
            (num1 + num2)
        }
    }
}

fun generateRandomNumber(start: Int, end: Int): Int {
    require(start <= end) { "Invalid range: start must be less than or equal to end" }
    return (start..end).random()
}
