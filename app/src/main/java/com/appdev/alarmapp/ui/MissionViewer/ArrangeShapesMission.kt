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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.convertToMilliseconds
import com.appdev.alarmapp.ui.AlarmCancel.playTextToSpeech
import com.appdev.alarmapp.ui.AlarmCancel.startCurrentTimeAndDate
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.setMaxVolume
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.MissionDemoHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertStringToSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.NoDragCancelledAnimation
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import kotlin.math.min

@Composable
fun ArrangeShapesMHScreen(
    intent: Intent = Intent(),
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    missionViewModel: MissionViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    timerEndsCallback: TimerEndsCallback,
    dismissCallback: DismissCallback
) {
    var oldMissionId by remember {
        mutableStateOf(mainViewModel.missionDetails.missionID)
    }
    val context = LocalContext.current
    val alarmEntity: AlarmEntity? by remember {
        mutableStateOf(intent.getParcelableExtra("Alarm"))
    }
    var timeIsDone by remember { mutableStateOf(alarmEntity?.isTimeReminder ?: false) }
    var speechIsDone by remember { mutableStateOf(alarmEntity?.isLabel ?: false) }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
    var progress by remember { mutableFloatStateOf(1f) }
    val elementsCountToPick by remember {
        mutableIntStateOf(mainViewModel.missionDetails.valuesToPick)
    }
    var countdown by remember { mutableStateOf(if (elementsCountToPick <= 6) 5 else 10) }
    var showWrong by remember { mutableStateOf(missionViewModel.missionHandler.notMatched) }

    var clickedNumbers by remember { mutableStateOf(emptyList<Int>()) }

    var correctList = remember { mutableStateOf((listOf(Icons.Filled.Circle,Icons.Filled.ChangeHistory,Icons.Filled.Hexagon,Icons.Filled.Pentagon,Icons.Filled.Square,Icons.Filled.Favorite,Icons.Filled.StarRate,Icons.Filled.LocalFireDepartment,Icons.Filled.WaterDrop,Icons.Filled.EmojiEmotions,Icons.Filled.Diamond,Icons.Filled.Cookie)).shuffled().take(elementsCountToPick)) }
    var shuffledList = remember { mutableStateOf(correctList.value.shuffled()) }
    val state =
        rememberReorderableLazyGridState(dragCancelledAnimation = NoDragCancelledAnimation(),
            onMove = { from, to ->
                shuffledList.value = shuffledList.value.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
                progress = 1f
            })


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
    LaunchedEffect(key1 = showWrong, key2 = missionViewModel.missionHandler.notMatched) {
        if (missionViewModel.missionHandler.notMatched) {
            delay(500)
            missionViewModel.missionEventHandler(MissionDemoHandler.updateMatch(false))
            showWrong = false
        }
    }
    LaunchedEffect(
        key1 = countdown,
        key2 = shuffledList,
        key3 = correctList
    ) {
        if (correctList.value.isEmpty() && countdown != 0) {
            correctList.value = listOf(Icons.Filled.Circle,Icons.Filled.Rectangle,Icons.Filled.Hexagon,Icons.Filled.Pentagon,Icons.Filled.Square,Icons.Filled.Favorite,Icons.Filled.StarRate,Icons.Filled.LocalFireDepartment,Icons.Filled.WaterDrop,Icons.Filled.EmojiEmotions,Icons.Filled.Diamond,Icons.Filled.Cookie).shuffled().take(elementsCountToPick)
            shuffledList.value = correctList.value.shuffled()
        }
    }


    //---------------------------------DESIGN--------------------------------
    Scaffold(bottomBar = {
        if (countdown == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                contentAlignment = Alignment.Center
            ) {
                CustomButton(
                    onClick = {
                        if (shuffledList.value.zip(correctList.value)
                                .all { (a, b) -> a == b } && mainViewModel.missionDetails.repeatProgress != mainViewModel.missionDetails.repeatTimes && oldMissionId == mainViewModel.missionDetails.missionID
                        ) {
                            clickedNumbers = emptyList()
                            correctList.value = emptyList()
                            shuffledList.value = emptyList()
                            countdown = if (elementsCountToPick <= 6) 5 else 10
                            mainViewModel.missionData(
                                MissionDataHandler.MissionProgress(
                                    mainViewModel.missionDetails.repeatProgress + 1
                                )
                            )
                        } else if (shuffledList.value.zip(correctList.value)
                                .all { (a, b) -> a == b } && mainViewModel.missionDetails.repeatProgress == mainViewModel.missionDetails.repeatTimes
                        ) {
                            if (mainViewModel.isRealAlarm || previewMode) {
                                val mutableList = mainViewModel.dummyMissionList.toMutableList()
                                mutableList.removeFirst()
                                mainViewModel.dummyMissionList = mutableList
                                missionViewModel.missionEventHandler(MissionDemoHandler.ResetData)
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
                                controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop
                                }
                            }
                        } else {
                            showWrong = true
                        }
                    },
                    text = "Verify Order",
                    width = 0.85f
                )
            }
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (countdown != 0) Color(0xff232E4C) else Color(0xff121315))
                .padding(it),
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

                Text(
                    text = if (showWrong && countdown == 0) "Order is not correct ! Try again" else if (countdown != 0) "Memorize!" + if (countdown > 0) " $countdown" else " " else "Arrange numbers in order",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp)
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    if (countdown != 0) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3), modifier = Modifier
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(correctList.value) { item ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.1f)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = item,
                                        contentDescription = "",
                                        tint = Color.White, modifier = Modifier.size(45.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = state.gridState,
                            modifier = Modifier.reorderable(state)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(shuffledList.value, { it.hashCode() }) { item ->
                                ReorderableItem(
                                    state,
                                    key = item.hashCode(),
                                    defaultDraggingModifier = Modifier, orientationLocked = false
                                ) { isDragging ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1.1f)
                                            .padding(10.dp)
                                            .background(Color(0xff1C1F26)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = item,
                                            contentDescription = "",
                                            tint = Color.White, modifier = Modifier.detectReorderAfterLongPress(state).size(45.dp)
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
}