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
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertStringToSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min


@Composable
fun AlterMissionScreen(
    intent: Intent = Intent(),
    textToSpeech: TextToSpeech,
    mainViewModel: MainViewModel,
    controller: NavHostController, timerEndsCallback: TimerEndsCallback,
    dismissCallback: DismissCallback,
    missionViewModel: MissionViewModel = hiltViewModel(),
) {

    var colors by remember {
        mutableStateOf(
            listOf(
                Color.Red, Color.Blue, Color.Magenta, Color.Green
            )
        )
    }
    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    var progress by remember { mutableFloatStateOf(1f) }
    var remainingTimes by remember { mutableStateOf(100) }

    val context = LocalContext.current
    val alarmEntity: AlarmEntity? by remember {
        mutableStateOf(intent.getParcelableExtra("Alarm"))
    }
    var timeIsDone by remember { mutableStateOf(alarmEntity?.isTimeReminder ?: false) }
    var speechIsDone by remember { mutableStateOf(alarmEntity?.isLabel ?: false) }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var showWrong by remember { mutableStateOf(false) }

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
    var isEnd by remember { mutableStateOf(false) }


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
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val newVolume = (it.customVolume / 100f * maxVolume).toInt()

                // Ensure the new volume is within the valid range (0 to maxVolume)
                val clampedVolume = newVolume.coerceIn(0, maxVolume)
                Log.d("CHKMUS", "$clampedVolume is the volume of music now")

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0)
            }
        }
        onDispose {}
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
                Helper.updateCustomValue(it.customVolume)
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

    LaunchedEffect(key1 = remainingTimes) {
        if ((mainViewModel.isRealAlarm || previewMode) && remainingTimes <=0 ) {
            val mutableList = mainViewModel.dummyMissionList.toMutableList()
            mutableList.removeFirst()
            mainViewModel.dummyMissionList = mutableList
            if(mainViewModel.dummyMissionList.isEmpty()){
                isEnd = true
                delay(2000)
            }
            if (mainViewModel.dummyMissionList.isNotEmpty()) {
                Log.d("CHKVM", "Value before ${mainViewModel.missionDetails.repeatProgress}")
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
                        if(Utils(context).areSnoozeTimersEmpty() && !previewMode && !Utils(context).isVolumeEmpty()){
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Utils(context).getCurrentVolume(), 0)
                            Utils(context).removeVolume()
                        }
                        Helper.stopStream()
                        textToSpeech.stop()
                        vibrator.cancel()
                        dismissCallback.onDismissClicked()
                    }
                }
            } else {
                if(Utils(context).areSnoozeTimersEmpty() && !previewMode && !Utils(context).isVolumeEmpty()){
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Utils(context).getCurrentVolume(), 0)
                    Utils(context).removeVolume()
                }
                Helper.stopStream()
                textToSpeech.stop()
                vibrator.cancel()
                dismissCallback.onDismissClicked()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff121315)),
        contentAlignment = Alignment.TopCenter
    ) {
        when (isEnd) {
            true -> {
                if ((mainViewModel.isRealAlarm || previewMode) && mainViewModel.dummyMissionList.isEmpty() && missionViewModel.missionHandler.preservedIndexes.isNotEmpty() && (missionViewModel.missionHandler.correctChoiceList.size == missionViewModel.missionHandler.preservedIndexes.size) && mainViewModel.missionDetails.repeatProgress == mainViewModel.missionDetails.repeatTimes) {
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
                                imageVector = Icons.Filled.Close,
                                contentDescription = "",
                                tint = Color.White, modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(vertical = 40.dp)) {
                        Text(
                            text = "Emergency dismiss",
                            color = Color.White,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W500,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp)
                        )
                        Text(
                            "Tap on green button to decrease count leading towards dismiss of alarm",
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            color = Color(0xffA6ACB5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp, start = 10.dp, end = 10.dp)
                        )
                    }
                    Text(
                        "$remainingTimes times",
                        fontSize = 40.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffb5c7ca),
                        textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth()
                    )
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            items(colors.size) { index ->
                                RubikCube(
                                    color = colors[index],
                                    onClick = {
                                        if(remainingTimes>0){
                                            if (colors[index] == Color.Green) {
                                                remainingTimes--
                                                showWrong = false
                                            } else{
                                                showWrong = true
                                            }
                                            progress = 1f
                                            colors = generateNewColors()
                                        }
                                    }
                                )
                            }
                            item{
                                Text(
                                    text = if(showWrong) "Tap on green Button !" else "",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.W500,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateNewColors(): List<Color> {
    val colors = listOf(Color.Red, Color.Blue, Color.Magenta, Color.Green)
    return colors.shuffled().toMutableList()
}

@Composable
fun RubikCube(
    color: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color)
            .size(75.dp)
            .clickable(onClick = onClick)
    )
}