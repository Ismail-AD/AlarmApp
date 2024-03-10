package com.appdev.alarmapp.ui.AlarmCancel

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.AlarmManagement.DismissCallback
import com.appdev.alarmapp.AlarmManagement.SnoozeCallback
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.setMaxVolume
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertMillisToHoursAndMinutes
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.getFormattedToday
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmCancelScreen(
    onDismissCallback: DismissCallback,
    snoozeCallback: SnoozeCallback ,
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel,
    intent: Intent = Intent()
) {
    val context = LocalContext.current
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var startItNow by remember { mutableStateOf(false) }
    var timeIsDone by remember { mutableStateOf(false) }
    var speechIsDone by remember { mutableStateOf(false) }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    Log.d("CHKMUS","$currentVolume is mobile's volume at this time")
    val scope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )
    }
    BackHandler {

    }

    val previewMode by remember {
        mutableStateOf(intent.getBooleanExtra("Preview", false))
    }

    var alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, mainViewModel))
    }
    var ringtone by remember {
        mutableStateOf(Ringtone())
    }
    var showSnoozed by remember {
        mutableStateOf(false)
    }

    val dismissSettingsReceived: DismissSettings? by remember {
        mutableStateOf(intent.getParcelableExtra("dismissSet"))
    }
    val alarmEntity: AlarmEntity? by remember {
        mutableStateOf(intent.getParcelableExtra("Alarm"))
    }



    DisposableEffect(key1 = Unit) {
        alarmEntity?.let {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = (it.customVolume / 100f * maxVolume).toInt()

            // Ensure the new volume is within the valid range (0 to maxVolume)
            val clampedVolume = newVolume.coerceIn(0, maxVolume)
            Log.d("CHKMUS","$clampedVolume is the volume of music now")

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0)
        }
        onDispose {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
            textToSpeech.stop()
            vibrator.cancel()
            if (isDarkMode) {
                systemUiController.setSystemBarsColor(
                    color = Color.Black,
                    darkIcons = false
                )
            } else {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = true
                )
            }
        }
    }
    LaunchedEffect(key1 = showSnoozed) {
        if (showSnoozed) {
            alarmEntity?.let {
                if (it.snoozeTime != -1) {
                    Toast.makeText(
                        context,
                        "Alarm is snoozed for ${it.snoozeTime} minutes",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit, key2 = timeIsDone, key3 = speechIsDone) {


        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }

            override fun onDone(utteranceId: String?) {
                startItNow = true
            }

            override fun onError(utteranceId: String?) {
            }
        })
        alarmEntity?.let {
            Helper.updateCustomValue(it.customVolume)
            Log.d("CHKMUS","${it.customVolume} is custom volume now")
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
        }
        dismissSettingsReceived?.let { dset ->
            if (dset.dismissTime > 0 && mainViewModel.isRealAlarm) {
                delay(dset.dismissTime * 60 * 1000L) // Convert minutes to milliseconds
                Helper.stopStream()
                textToSpeech.stop()
                vibrator.cancel()
                textToSpeech.shutdown()
                onDismissCallback.onDismissClicked()
            }
        }
    }

    LaunchedEffect(key1 = Unit){
        if(!mainViewModel.isRealAlarm && !previewMode){
            Log.d("CHKMUS","Mission Viewer Music Started")
            Helper.playStream(context, R.raw.alarmsound)
        }

        Log.d("CHKMUS","IS MUSIC PLAYING BEFORE GOING TO PLAY ${Helper.isPlaying()}")
        if ((mainViewModel.isRealAlarm || previewMode)) {
            alarmEntity?.let { alarm ->
                if (alarm.ringtone.rawResourceId != -1) {
                    Log.d("CHKMUS","ID CHECK for resource ${alarm.ringtone.rawResourceId != -1}")
                    ringtone = ringtone.copy(rawResourceId = alarm.ringtone.rawResourceId)
                    if (alarm.isTimeReminder) {
                        if (!startItNow) {
                            scope.launch {
                                delay(500)
                                startCurrentTimeAndDate(
                                    alarm.labelTextForSpeech,
                                    textToSpeech,
                                    System.currentTimeMillis().toString() + (0..19992).random()
                                )
                            }
                        }
                    }

                    if (!alarm.isTimeReminder) {
                        if (alarm.isLabel) {
                            scope.launch {
                                delay(500)
                                playTextToSpeech(
                                    text = alarm.labelTextForSpeech,
                                    textToSpeech = textToSpeech,
                                    id = System.currentTimeMillis().toString() + (0..19992).random()
                                )
                            }
                        }
                        if (alarm.isGentleWakeUp) {
                            Log.d("CHKMUS","IS GENTLE WAKE-UP")
                            Helper.updateLow(true)
                            Helper.startIncreasingVolume(
                                convertToMilliseconds(
                                    if (alarm.wakeUpTime <= 10) alarm.wakeUpTime else 0,
                                    if (alarm.wakeUpTime > 10) alarm.wakeUpTime else 0
                                )
                            )
                        }

                        Helper.playStream(context.applicationContext, alarm.ringtone.rawResourceId)
                    } else {
                        if (startItNow) {
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
                        }
                    }
                    if (alarm.isLoudEffect) {
                        scope.launch {
                            delay(40000L)
                            setMaxVolume(context)
                            Helper.playStream(context, R.raw.loudeffect)
                        }
                    }
                } else if (alarm.ringtone.uri != null) {
                    ringtone = ringtone.copy(uri = alarm.ringtone.uri)

                    if (alarm.isTimeReminder) {
                        if (!startItNow) {
                            scope.launch {
                                delay(500)
                                startCurrentTimeAndDate(
                                    alarm.labelTextForSpeech,
                                    textToSpeech,
                                    System.currentTimeMillis().toString()
                                )
                            }
                        }
                    }

                    if (!alarm.isTimeReminder) {
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
                    } else {
                        if (startItNow) {
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
                        }
                    }

                    if (alarm.isLoudEffect) {
                        scope.launch {
                            delay(40000L)
                            setMaxVolume(context)
                            Helper.playStream(context, R.raw.loudeffect)
                        }
                    }


                } else if (alarm.ringtone.file != null) {
                    ringtone = ringtone.copy(file = alarm.ringtone.file)

                    if (alarm.isTimeReminder) {
                        if (!startItNow) {
                            scope.launch {
                                delay(500)
                                startCurrentTimeAndDate(
                                    alarm.labelTextForSpeech,
                                    textToSpeech,
                                    System.currentTimeMillis().toString()
                                )
                            }
                        }
                    }
                    if (!alarm.isTimeReminder) {
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
                    } else {
                        if (startItNow) {
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
                        }
                    }
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
//    if (mainViewModel.isRealAlarm && !Helper.isPlaying()) {
//        when (intent.getIntExtra("tonetype", 0)) {
//            0 -> {
//                val rawResourceId = intent.getStringExtra("ringtone")
//                rawResourceId?.let {
//                    ringtone = ringtone.copy(rawResourceId = it.toInt())
//                    Helper.playStream(context, it.toInt())
//                }
//            }
//
//            1 -> {
//                val uriString = intent.getStringExtra("ringtone")
//                val uri = Uri.parse(uriString)
//                ringtone = ringtone.copy(uri = uri)
//                Helper.playStream(context, uri = uri)
//            }
//
//            2 -> {
//                val filePath = intent.getStringExtra("ringtone")
//                filePath?.let {
//                    val file = File(it)
//                    ringtone = ringtone.copy(file = file)
//                    Helper.playFile(file, context)
//                }
//            }
//
//            else -> {}
//        }
//    } else {
//        if (!Helper.isPlaying()) {
//            Helper.playStream(context, R.raw.alarmsound)
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor), contentAlignment = Alignment.TopCenter
    ) {
        Log.d("GOINGTO","In COMPOSABLE ${Helper.isPlaying()}")

        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 35.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    convertMillisToHoursAndMinutes(System.currentTimeMillis()),
                    fontSize = 80.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xffb5c7ca),
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W600
                )
                Text(
                    getFormattedToday(), fontSize = 25.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xffb5c7ca), textAlign = TextAlign.Center
                )

            }

            alarmEntity?.let {

                if (it.snoozeTime != -1 && mainViewModel.isRealAlarm) {
                    CustomButton(
                        onClick = {
                            val notifyIt = intent.getBooleanExtra("notify", false)

                            mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = it.willVibrate))
                            mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = it.customVolume))
                            mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = it.isLabel))
                            mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = it.labelTextForSpeech))
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.LoudEffect(
                                    isLoudEffectOrNot = it.isLoudEffect
                                )
                            )
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.TimeReminder(
                                    isTimeReminderOrNot = it.isTimeReminder
                                )
                            )
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.IsGentleWakeUp(
                                    isGentleWakeUp = it.isGentleWakeUp
                                )
                            )
                            mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = it.wakeUpTime))
                            mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = it.isActive))
                            mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = it.listOfDays))
                            mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = it.ringtone))
                            mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = it.skipTheAlarm))
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.isOneTime(isOneTime = it.isOneTime)
                            )
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.getTime(
                                    time = it.localTime
                                )
                            )
                            mainViewModel.missionData(MissionDataHandler.AddList(missionsList = it.listOfMissions))
                            mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = it.id))
                            mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(isOneTime = it.isOneTime))
                            mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = it.timeInMillis))
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.getSnoozeTime(
                                    getSnoozeTime = it.snoozeTime
                                )
                            )
                            snoozeAlarm(
                                it, alarmScheduler, notifyIt, mainViewModel
                            )
                            startItNow = false
                            showSnoozed = true
                            textToSpeech.stop()
                            textToSpeech.shutdown()
                            vibrator.cancel()
                            Helper.stopStream()
//                            controller.navigate(Routes.SnoozeScr.route) {
//                                popUpTo(Routes.PreviewAlarm.route) {
//                                    inclusive = true
//                                }
//                                launchSingleTop = true
//                            }
                            snoozeCallback.onSnoozeClicked()
                        },
                        text = "Snooze",
                        backgroundColor = Color(0xfff18d44),
                        textColor = Color.White,
                        width = 0.6f, height = 60.dp
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                CustomButton(
                    onClick = {
                        if (mainViewModel.isRealAlarm || previewMode) {
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
                                controller.navigate(Routes.MissionScreen.route)
                            }

                            "Shake" -> {
                                controller.navigate(Routes.MissionShakeScreen.route)
                            }

                            "Math" -> {
                                controller.navigate(Routes.MissionMathScreen.route)
                            }

                            "Typing" -> {
                                controller.navigate(Routes.TypingPreviewScreen.route)
                            }

                            "Step" -> {
                                controller.navigate(Routes.StepDetectorScreen.route)
                            }

                            "Squat" -> {
                                controller.navigate(Routes.SquatMissionScreen.route)
                            }

                            "Photo" -> {
                                controller.navigate(Routes.PhotoMissionPreviewScreen.route)
                            }

                            "QR/Barcode" -> {
                                controller.navigate(Routes.BarCodePreviewAlarmScreen.route)
                            }

                            else -> {
                                Helper.stopStream()
                                textToSpeech.stop()
                                vibrator.cancel()
                                textToSpeech.shutdown()
                                Log.d(
                                    "CHKSM",
                                    "ALARM IS GOING TO END AS DISMISSED IS CLICKED............."
                                )
//                                if(!mainViewModel.isRealAlarm && !previewMode){
//
//                                }
                                onDismissCallback.onDismissClicked()
                            }
                        }

                    },
                    text = if (mainViewModel.dummyMissionList.isNotEmpty()) "Start the mission" else "Dismiss",
                    height = 70.dp,
                    width = 0.9f, textColor = Color.White
                )
            }
        }
        if (!mainViewModel.isRealAlarm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp), contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp)
                            .padding(horizontal = 20.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(width = 2.dp, color = Color(0xffA6ACB5))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent, CircleShape)
                                .clickable {
                                    Helper.stopStream()
                                    textToSpeech.stop()
                                    vibrator.cancel()
                                    if (previewMode) {
                                        controller.navigate(Routes.MainScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    } else {
                                        controller.popBackStack()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Exit Preview Mode",
                                fontSize = 16.sp,
                                letterSpacing = 0.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }

//                CustomImageButton(
//                    onClick = {
//                        Helper.stopStream()
//                        textToSpeech.stop()
//                        vibrator.cancel()
//                        if (previewMode) {
//                            controller.navigate(Routes.MainScreen.route) {
//                                popUpTo(controller.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
//                        } else {
//                            controller.navigate(Routes.MissionMenuScreen.route) {
//                                popUpTo(controller.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
//                        }
//                    },
//                    text = "Exit Preview Mode",
//                    height = 60.dp,
//                    width = 1.0f,
//                    icon = Icons.Filled.Close,
//                    iconColor = Color.Red,
//                    backgroundColor = Color.White,
//                    textColor = Color.Black,
//                    roundedCornerShape = RoundedCornerShape(0.dp)
//                )
            }
        }
    }
}

fun snoozeAlarm(
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
    showInNotification: Boolean,
    mainViewModel: MainViewModel
) {
    val snoozeMinutes =
        alarmEntity.snoozeTime // Set the snooze duration in minutes (adjust as needed)
    val currentTimeMillis = System.currentTimeMillis()
    Log.d("CHKNB","CURRENT TIME : ${convertMillisToLocalTime(currentTimeMillis)}")

    val snoozeTimeMillis = currentTimeMillis + (snoozeMinutes * 60 * 1000)
    Log.d("CHKNB","TIME OF TRIGGER: ${convertMillisToLocalTime(snoozeTimeMillis)}")
    mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = snoozeTimeMillis))
    mainViewModel.updateHandler(EventHandlerAlarm.update)

    alarmEntity.snoozeTimeInMillis = snoozeTimeMillis

//    val instant = Instant.ofEpochMilli(snoozeTimeMillis)
//    val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
//    alarmEntity.localTime = offsetTime.toLocalTime()
//    Log.d("RINGC","LOCAL TIME AFTER SNOOZE ADD ${offsetTime.toLocalTime()}")

    // Reschedule the alarm with the updated time
    alarmScheduler.schedule(alarmEntity, showInNotification)
}


fun playTextToSpeech(textToSpeech: TextToSpeech, id: String, text: String) {

    val params = Bundle()
    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)

    // Play the formatted time and date as audio
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
}

fun startCurrentTimeAndDate(text: String, textToSpeech: TextToSpeech, id: String) {
    val params = Bundle()
    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDateAndTime: String = sdf.format(Date())

    // Extracting only hour, minute, date, and day
    val formattedTimeAndDate = SimpleDateFormat("hh:mm a EEEE, MMMM d, yyyy", Locale.getDefault())
        .format(sdf.parse(currentDateAndTime) ?: Date())

    // Play the formatted time and date as audio
    textToSpeech.speak(formattedTimeAndDate + text, TextToSpeech.QUEUE_FLUSH, params, id)
}


fun convertToMilliseconds(minutes: Int, seconds: Int): Long {
    return (minutes * 60 + seconds) * 1000L
}