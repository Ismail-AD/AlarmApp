package com.appdev.alarmapp.ui.MissionViewer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.setMaxVolume
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.convertStringToSet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StepMission(
    intent: Intent = Intent(),
    textToSpeech: TextToSpeech,
    mainViewModel: MainViewModel,
    controller: NavHostController, timerEndsCallback: TimerEndsCallback,
    dismissCallback: DismissCallback
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()

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
    var isEnd by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(10) }

    val locPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showRationale by remember(locPermissionState) {
        mutableStateOf(false)
    }
    var startUpdating by remember(locPermissionState) {
        mutableStateOf(false)
    }

    // FusedLocationProviderClient for obtaining location updates
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // MutableState for storing the previous location
    var previousLocation by remember { mutableStateOf<Location?>(null) }

    var progress by remember { mutableFloatStateOf(1f) }

    var stepsToBeDone by remember { mutableStateOf(mainViewModel.missionDetails.repeatTimes) }

    var isLocationEnabled by remember {
        mutableStateOf(locationEnabled(context))
    }
    val sensorManager by remember {
        mutableStateOf(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
    }
    val stepSensor by remember {
        mutableStateOf(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    }
    val stepDetector by remember {
        mutableStateOf(StepDetector())
    }

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value
    LaunchedEffect(key1 = Unit) {
        if (dismissSettings.muteTone) {
            Helper.stopStream()
            vibrator.cancel()
            textToSpeech.stop()
        }
        if(!locationEnabled(context = context)){
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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
        onDispose {
//            if (!dismissSettings.muteTone && !Helper.isPlaying()) {
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
//            }
        }
    }


    LaunchedEffect(key1 = Unit, key2 = timeIsDone, key3 = speechIsDone) {
        Log.d("CHKSP", "Speech Begins and values are $timeIsDone  and $speechIsDone")
        if (!dismissSettings.muteTone && !Helper.isPlaying()){

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
        if(!dismissSettings.muteTone && !Helper.isPlaying()){
            if (!mainViewModel.isRealAlarm && !previewMode) {
                Log.d("CHKMUS", "Mission Viewer Music Started")
                Helper.playStream(context, R.raw.alarmsound)
            }

            Log.d("CHKMUS", "IS MUSIC PLAYING BEFORE GOING TO PLAY ${Helper.isPlaying()}")
            if ((mainViewModel.isRealAlarm || previewMode) && !timeIsDone && !speechIsDone) {
                alarmEntity?.let { alarm ->
                    if (alarm.ringtone.rawResourceId != -1) {
                        Log.d("CHKMUS", "ID CHECK for resource ${alarm.ringtone.rawResourceId != -1}")
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
            if(!mainViewModel.isRealAlarm){
                controller.popBackStack()
            } else{
                if(!mainViewModel.isSnoozed){
                    controller.navigate(Routes.PreviewAlarm.route) {
                        mainViewModel.dummyMissionList = emptyList()
                        mainViewModel.dummyMissionList = mainViewModel.missionDetailsList
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
                scope.launch {
                    delay(1000)
                    countdown--
                }
            } else {
                startUpdating = true
                progress = 1f
            }
    }

    DisposableEffect(key1 = Unit,key2 = startUpdating) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation.let { location ->
                    // Handle the new location
                    if (previousLocation != null) {
                        Log.d("CHKLOC", "old location $previousLocation")

                        // Calculate distance between previous and current location
                        val distance = calculateDistance(previousLocation!!, location)

                        Log.d("CHKLOC", "distance calculated $distance")
                        if (distance > 1.0 && startUpdating) {
                            stepsToBeDone -= 1
                            previousLocation = location
                        }
                    } else {
                        // If previous location is null, update it with current location
                        previousLocation = location
                    }
                }
            }
        }

        val locationRequest = createLocationRequest()
        if (locPermissionState.status.shouldShowRationale) {
            showRationale = true
        } else if (locPermissionState.status.isGranted) {
            Log.d("CHKLOC", "Request for updates raised")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback, Looper.getMainLooper()
            )
        } else {
            locPermissionState.launchPermissionRequest()
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    LaunchedEffect(key1 = stepsToBeDone) {
        if (stepsToBeDone <= 0) {
            if (mainViewModel.isRealAlarm || previewMode) {
                val mutableList = mainViewModel.dummyMissionList.toMutableList()
                mutableList.removeFirst()
                mainViewModel.dummyMissionList = mutableList
                if(mainViewModel.dummyMissionList.isEmpty()){
                    isEnd = true
                    delay(2000)
                }
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
            } else {
                if(!mainViewModel.isSnoozed){
                    controller.navigate(Routes.PreviewAlarm.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else{
                    timerEndsCallback.onTimeEnds()
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
        when(isEnd){
            true->{
                if(stepsToBeDone <= 0 && mainViewModel.dummyMissionList.isEmpty() && (mainViewModel.isRealAlarm || previewMode)){
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
            }else->{
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
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.35f)
                        .padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
//                    if (!isLocationEnabled) {
//                        Text(
//                            text = "Turn the location on in order to start the mission !",
//                            color = Color.White,
//                            fontSize = 23.sp,
//                            lineHeight = 32.sp,
//                            fontWeight = FontWeight.W400,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.padding(horizontal = 15.dp)
//                        )
//                        Spacer(modifier = Modifier.height(40.dp))
//                        CustomButton(onClick = {
//                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                            isLocationEnabled = locationEnabled(context)
//                        }, text = "Continue Step mission")
//                    } else {
                        Text(
                            text = if (countdown != 0) "1. Turn the location On If it is Off \n2. Stand up and take your position ! \n\nStarting in $countdown" else "Take Steps Softly",
                            color = Color.White,
                            fontSize = 23.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.W400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )
                        Text(
                            text = if (countdown != 0) "" else "$stepsToBeDone",
                            color = Color.White,
                            fontSize = 70.sp,
                            fontWeight = FontWeight.W700,
                            modifier = Modifier.padding(top = 20.dp),
                            letterSpacing = 3.sp
                        )
//                    }
                }

            }
            }
        }
        if (showRationale) {
            AlertDialog(
                onDismissRequest = {
                    showRationale = false
                },
                title = {
                    Text(
                        text = "Permissions required by the Application",
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                text = {
                    Text(
                        text = "The Application requires the permissions to work",
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRationale = false
                            locPermissionState.launchPermissionRequest()
                        },
                    ) {
                        Text("Continue", color = MaterialTheme.colorScheme.surfaceTint)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRationale = false
                        },
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.surfaceTint)
                    }
                },
            )
        }
    }
}

fun calculateDistance(location1: Location, location2: Location): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        location1.latitude, location1.longitude,
        location2.latitude, location2.longitude,
        result
    )
    return result[0]
}

fun deg2rad(deg: Double): Double {
    return deg * (PI / 180)
}

private fun createLocationRequest(): LocationRequest {
    return LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(1000) // Interval in milliseconds
        .setFastestInterval(200)
}

private fun locationEnabled(context: Context): Boolean {
    val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}