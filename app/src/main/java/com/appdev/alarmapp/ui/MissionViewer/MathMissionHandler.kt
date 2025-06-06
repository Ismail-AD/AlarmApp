package com.appdev.alarmapp.ui.MissionViewer

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.DismissCallback
import com.appdev.alarmapp.AlarmManagement.TimerEndsCallback
import com.appdev.alarmapp.AlarmManagement.Utils
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.convertToMilliseconds
import com.appdev.alarmapp.ui.AlarmCancel.playTextToSpeech
import com.appdev.alarmapp.ui.AlarmCancel.startCurrentTimeAndDate
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.setMaxVolume
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.MissionDemoHandler
import com.appdev.alarmapp.utils.MissionMathDemoHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertStringToSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMissionHandler(
    intent: Intent = Intent(),
    textToSpeech: TextToSpeech,
    mainViewModel: MainViewModel,
    missionLevel: String = "Very Easy",
    controller: NavHostController,
    missionViewModel: MissionViewModel = hiltViewModel(),
    timerEndsCallback: TimerEndsCallback,
    dismissCallback: DismissCallback
) {

    val context = LocalContext.current

    val alarmEntity: AlarmEntity? by remember {
        mutableStateOf(intent.getParcelableExtra("Alarm"))
    }
    var timeIsDone by remember { mutableStateOf(alarmEntity?.isTimeReminder ?: false) }
    var speechIsDone by remember { mutableStateOf(alarmEntity?.isLabel ?: false) }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    var ringtone by remember {
        mutableStateOf(Ringtone())
    }
    val scope = rememberCoroutineScope()
    val previewMode by remember {
        mutableStateOf(intent.getBooleanExtra("Preview", false))
    }

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    var isEnd by remember { mutableStateOf(false) }


    var progress by remember { mutableFloatStateOf(1f) }
    var countdown by remember { mutableStateOf(3) }
    var showWrong by remember { mutableStateOf(missionViewModel.missionMathHandler.notMatched) }
    var rightAnswer by remember { mutableStateOf(missionViewModel.missionMathHandler.answerCorrect) }
    var qusetionString by remember {
        mutableStateOf(getMathValues(missionLevel, missionViewModel))
    }
    var answer by remember { mutableStateOf("") }
    var oldMissionId by remember {
        mutableStateOf(mainViewModel.missionDetails.missionID)
    }

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

    LaunchedEffect(key1 = Unit) {
        if (dismissSettings.muteTone) {
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
        }
    }
    BackHandler {

    }
    DisposableEffect(key1 = Unit) {
        if (!dismissSettings.muteTone && !Helper.isPlaying()) {
            alarmEntity?.let {
                Helper.updateCustomValue(it.customVolume)

                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val newVolume = (it.customVolume / 100f * maxVolume).toInt()

                // Ensure the new volume is within the valid range (0 to maxVolume)
                val clampedVolume = newVolume.coerceIn(0, maxVolume)
                Log.d("CHKMUS", "$clampedVolume is the volume of music now")

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0)
            }
        }
        onDispose {
//            if (!dismissSettings.muteTone && !Helper.isPlaying()) {
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
//            }
        }
    }


    LaunchedEffect(key1 = Unit, key2 = timeIsDone, key3 = speechIsDone) {
        Log.d("CHKSP", "Speech Begins and values are $timeIsDone  and $speechIsDone")
        if (!dismissSettings.muteTone && !Helper.isPlaying()) {

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {

                }

                override fun onDone(utteranceId: String?) {
                    Log.d("CHKSP", "on Done called and values are $timeIsDone  and $speechIsDone")

                    if (timeIsDone) {
                        timeIsDone = false
                    } else if (speechIsDone) {
                        speechIsDone = false
                    }
                }

                override fun onError(utteranceId: String?) {
                }
            })
            alarmEntity?.let {
                Log.d("CHKMUS", "${it.customVolume} is custom volume now")
                if (it.willVibrate) {
                    vibrator.cancel()
                    val vibrationEffect = VibrationEffect.createWaveform(
                        longArrayOf(
                            0,
                            250
                        ), // Pattern for continuous vibration (0 indicates vibration, 1000 milliseconds off)
                        0 // Repeat at index 0
                    )
                    vibrator.vibrate(vibrationEffect)
                }
                if (it.isTimeReminder && timeIsDone) {
                    scope.launch {
                        delay(500)
                        startCurrentTimeAndDate(
                            textToSpeech,
                            System.currentTimeMillis().toString() + (0..19992).random()
                        )
                    }
                }
                if (it.isLabel && !timeIsDone && speechIsDone) {
                    scope.launch {
                        delay(500)
                        playTextToSpeech(
                            text = it.labelTextForSpeech,
                            textToSpeech = textToSpeech,
                            id = System.currentTimeMillis().toString() + (0..19992).random()
                        )
                    }
                }


            }
        }
    }

    LaunchedEffect(key1 = Unit, key2 = timeIsDone, key3 = speechIsDone) {
        Log.d("CHKSP", "Going to check to play tone and values are $timeIsDone  and $speechIsDone")
        if (!dismissSettings.muteTone && !Helper.isPlaying()) {
            if (!mainViewModel.isRealAlarm && !previewMode) {
                Log.d("CHKMUS", "Mission Viewer Music Started")
                Helper.playStream(context, R.raw.alarmsound)
            }

            Log.d("CHKMUS", "IS MUSIC PLAYING BEFORE GOING TO PLAY ${Helper.isPlaying()}")
            if ((mainViewModel.isRealAlarm || previewMode) && !timeIsDone && !speechIsDone) {
                alarmEntity?.let { alarm ->
                    if (alarm.ringtone.rawResourceId != -1) {
                        Log.d(
                            "CHKMUS",
                            "ID CHECK for resource ${alarm.ringtone.rawResourceId != -1}"
                        )
                        ringtone = ringtone.copy(rawResourceId = alarm.ringtone.rawResourceId)
                        if (alarm.isGentleWakeUp) {
                            Helper.updateLow(true)
                            Helper.startIncreasingVolume(
                                convertToMilliseconds(
                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
                                )
                            )
                        }
                        Helper.playStream(context, alarm.ringtone.rawResourceId)
//                    if (alarm.isTimeReminder) {
//                        if (!startItNow) {
//                            scope.launch {
//                                delay(500)
//                                startCurrentTimeAndDate(
//                                    alarm.labelTextForSpeech,
//                                    textToSpeech,
//                                    System.currentTimeMillis().toString() + (0..19992).random()
//                                )
//                            }
//                        }
//                    }

//                    if (!alarm.isTimeReminder) {
//                        if (alarm.isLabel) {
//                            scope.launch {
//                                delay(500)
//                                playTextToSpeech(
//                                    text = alarm.labelTextForSpeech,
//                                    textToSpeech = textToSpeech,
//                                    id = System.currentTimeMillis().toString() + (0..19992).random()
//                                )
//                            }
//                        }
//                        if (alarm.isGentleWakeUp) {
//                            Log.d("CHKMUS", "IS GENTLE WAKE-UP")
//                            Helper.updateLow(true)
//                            Helper.startIncreasingVolume(
//                                convertToMilliseconds(
//                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                )
//                            )
//                        }
//
//                        Helper.playStream(context.applicationContext, alarm.ringtone.rawResourceId)
//                    } else {
//                        if (startItNow) {
//                            if (alarm.isGentleWakeUp) {
//                                Helper.updateLow(true)
//                                Helper.startIncreasingVolume(
//                                    convertToMilliseconds(
//                                        if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                        if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                    )
//                                )
//                            }
//                            Helper.playStream(context, alarm.ringtone.rawResourceId)
//                        }
//                    }
                        if (alarm.isLoudEffect) {
                            scope.launch {
                                delay(40000L)
                                setMaxVolume(context)
                                Helper.playStream(context, R.raw.loudeffect)
                            }
                        }
                    } else if (alarm.ringtone.uri != null) {
                        ringtone = ringtone.copy(uri = alarm.ringtone.uri)
                        if (alarm.isGentleWakeUp) {
                            Helper.updateLow(true)
                            Helper.startIncreasingVolume(
                                convertToMilliseconds(
                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
                                )
                            )
                        }
                        Helper.playStream(context, uri = alarm.ringtone.uri)
//                    if (alarm.isTimeReminder) {
//                        if (!startItNow) {
//                            scope.launch {
//                                delay(500)
//                                startCurrentTimeAndDate(
//                                    alarm.labelTextForSpeech,
//                                    textToSpeech,
//                                    System.currentTimeMillis().toString()
//                                )
//                            }
//                        }
//                    }
//
//                    if (!alarm.isTimeReminder) {
//                        if (alarm.isGentleWakeUp) {
//                            Helper.updateLow(true)
//                            Helper.startIncreasingVolume(
//                                convertToMilliseconds(
//                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                )
//                            )
//                        }
//                        Helper.playStream(context, uri = alarm.ringtone.uri)
//                    } else {
//                        if (startItNow) {
//                            if (alarm.isGentleWakeUp) {
//                                Helper.updateLow(true)
//                                Helper.startIncreasingVolume(
//                                    convertToMilliseconds(
//                                        if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                        if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                    )
//                                )
//                            }
//                            Helper.playStream(context, uri = alarm.ringtone.uri)
//                        }
//                    }

                        if (alarm.isLoudEffect) {
                            scope.launch {
                                delay(40000L)
                                setMaxVolume(context)
                                Helper.playStream(context, R.raw.loudeffect)
                            }
                        }


                    } else if (alarm.ringtone.file != null) {
                        ringtone = ringtone.copy(file = alarm.ringtone.file)
                        if (alarm.isGentleWakeUp) {
                            Helper.updateLow(true)
                            Helper.startIncreasingVolume(
                                convertToMilliseconds(
                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
                                )
                            )
                        }
                        Helper.playFile(alarm.ringtone.file!!, context)
//                    if (alarm.isTimeReminder) {
//                        if (!startItNow) {
//                            scope.launch {
//                                delay(500)
//                                startCurrentTimeAndDate(
//                                    alarm.labelTextForSpeech,
//                                    textToSpeech,
//                                    System.currentTimeMillis().toString()
//                                )
//                            }
//                        }
//                    }
//                    if (!alarm.isTimeReminder) {
//                        if (alarm.isGentleWakeUp) {
//                            Helper.updateLow(true)
//                            Helper.startIncreasingVolume(
//                                convertToMilliseconds(
//                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                )
//                            )
//                        }
//                        Helper.playFile(alarm.ringtone.file!!, context)
//                    } else {
//                        if (startItNow) {
//                            if (alarm.isGentleWakeUp) {
//                                Helper.updateLow(true)
//                                Helper.startIncreasingVolume(
//                                    convertToMilliseconds(
//                                        if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
//                                        if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
//                                    )
//                                )
//                            }
//                            Helper.playFile(alarm.ringtone.file!!, context)
//                        }
//                    }
                        if (alarm.isLoudEffect) {
                            scope.launch {
                                delay(40000L)
                                setMaxVolume(context)
                                Helper.playStream(context, R.raw.loudeffect)
                            }
                        }

                    } else {
                    }
                }
            }
        }
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
            if (mainViewModel.isRealAlarm || previewMode) {
                val mutableList = mainViewModel.dummyMissionList.toMutableList()
                mutableList.removeFirst()
                mainViewModel.dummyMissionList = mutableList
                missionViewModel.missionMathEventHandler(MissionMathDemoHandler.ResetData)

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
                            codeId = singleMission.codeId, locId = singleMission.locId, valuesToPick = singleMission.valuesToPick
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
                        "RangeNumbers" -> {
                            controller.navigate(Routes.RangeMemoryMissionPreview.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "RangeAlphabet" -> {
                            controller.navigate(Routes.RangeAlphabetMissionPreview.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "WalkOff" -> {
                            controller.navigate(Routes.WalkOffScreen.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "ReachDestination" -> {
                            controller.navigate(Routes.AtLocationMissionScreen.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "ArrangeNumbers" -> {
                            controller.navigate(Routes.ArrangeNumbersScreen.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "ArrangeAlphabet" -> {
                            controller.navigate(Routes.ArrangeAlphabetsScreen.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "ArrangeShapes" -> {
                            controller.navigate(Routes.ArrangeShapesScreen.route){
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        else -> {
                            Helper.stopStream()
                            textToSpeech.stop()
                            vibrator.cancel()
                            dismissCallback.onDismissClicked()
                        }
                    }
                } else {
                    Helper.stopStream()
                    textToSpeech.stop()
                    vibrator.cancel()
                    dismissCallback.onDismissClicked()
                }
            } else {
                controller.navigate(Routes.CommonMissionScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop
                }
            }
        }
        if (missionViewModel.missionMathHandler.answerCorrect && mainViewModel.missionDetails.repeatProgress != mainViewModel.missionDetails.repeatTimes && oldMissionId == mainViewModel.missionDetails.missionID) {
            delay(550)
            Log.d("CHKVM", "I AM CALLED")
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
            if (!mainViewModel.isRealAlarm) {
                controller.popBackStack()
            } else {
                if (!mainViewModel.isSnoozed) {
                    mainViewModel.dummyMissionList = emptyList()
                    mainViewModel.dummyMissionList = mainViewModel.missionDetailsList
                    controller.navigate(Routes.PreviewAlarm.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else {
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
                    mainViewModel.dummyMissionList = emptyList()
                    mainViewModel.dummyMissionList = mainViewModel.missionDetailsList
                    if (!mainViewModel.isRealAlarm) {
                        controller.popBackStack()
                    } else {
                        if (!mainViewModel.isSnoozed) {
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        } else {
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
                                        StringBuilder(answer).delete(6, answer.length)
                                            .toString()
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

fun getMathValues(value: String, missionViewModel: MissionViewModel): String {
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
