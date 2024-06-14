package com.appdev.alarmapp.ui.MissionViewer

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.appdev.alarmapp.ui.MissionDemos.bitmapDescriptorFromVector
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AtLocationMission(
    intent: Intent = Intent(),
    textToSpeech: TextToSpeech,
    mainViewModel: MainViewModel,
    controller: NavHostController, timerEndsCallback: TimerEndsCallback,
    dismissCallback: DismissCallback
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient by remember {
        mutableStateOf(LocationServices.getFusedLocationProviderClient(context))
    }
    var locationName by remember { mutableStateOf("Unknown Location") }
    val currentLocation by mainViewModel.currentLocation.collectAsStateWithLifecycle()
    val loaderState by mainViewModel.isFetchingLocation.collectAsStateWithLifecycle()
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
    var countdown by remember { mutableStateOf(10) }

    val locPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showRationale by remember(locPermissionState) {
        mutableStateOf(false)
    }

    var isMatched by remember { mutableStateOf<Boolean?>(null) }


    var progress by remember { mutableFloatStateOf(1f) }

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var showDialog by remember(permissionState) {
        mutableStateOf(false)
    }
    var dataToBeMatched by remember {
        mutableStateOf(mainViewModel.selectedLocationByName.locationString)
    }
    var markerState = remember { MarkerState(position = LatLng(0.0, 0.0)) }


    val singapore = LatLng(1.35, 103.87)
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 30f)
    }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var mapProperties by remember { mutableStateOf(MapProperties()) }
    var calculatedLocation by remember { mutableStateOf<LatLng?>(null) }


    LaunchedEffect(key1 = Unit, key2 = mainViewModel.selectedLocationByName) {
        if (mainViewModel.missionDetails.locId > 0) {
            mainViewModel.getLocationById(mainViewModel.missionDetails.locId)
        } else {
            calculatedLocation = LatLng(mainViewModel.longitude, mainViewModel.latitude)
        }

        if (mainViewModel.selectedLocationByName.locationString.trim().isNotEmpty()) {
            calculatedLocation = LatLng(
                mainViewModel.selectedLocationByName.longitude,
                mainViewModel.selectedLocationByName.latitude
            )
        }

    }
    LaunchedEffect(key1 = calculatedLocation) {
        calculatedLocation?.let {
            markerState = MarkerState(it)
        }
    }
    LaunchedEffect(key1 = loaderState) {
        if (!loaderState && currentLocation != null) {
            currentLocation?.let {
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 18f)
            }
        }
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
                    controller.navigate(Routes.PreviewAlarm.route) {
                        mainViewModel.dummyMissionList = emptyList()
                        mainViewModel.dummyMissionList = mainViewModel.missionDetailsList
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                } else {
                    timerEndsCallback.onTimeEnds()
                }
            }
        }
    }
    var startUpdating by remember(locPermissionState) {
        mutableStateOf(false)
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
    var totalDistance by remember {
        mutableStateOf(0f)
    }
    var updateCounter by remember {
        mutableStateOf(3)
    }
    var currentDistance by remember {
        mutableStateOf(FloatArray(1))
    }
    var startPoint by remember {
        mutableStateOf<Location?>(null)
    }

    DisposableEffect(key1 = Unit, key2 = startUpdating) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation.let { location ->
                    location?.let {
                        if (currentLocation != null) {
                            isMatched = LatLng(it.latitude, it.longitude) == LatLng(
                                calculatedLocation!!.latitude,
                                calculatedLocation!!.longitude
                            )
                            mainViewModel.updateCurrentLocation(it)
                            if (startPoint != null) {
                                Log.d("CHKDIS", "UP: $updateCounter")
                                if (updateCounter > 0) {
                                    updateCounter--
                                } else {
                                    Location.distanceBetween(
                                        startPoint!!.latitude, startPoint!!.longitude,
                                        it.latitude, it.longitude,
                                        currentDistance
                                    )
                                    totalDistance = currentDistance[0]
                                    Log.d("CHKDIS", "Dis: $totalDistance")

                                    if (totalDistance > 2f) {
                                        startPoint = it
                                        totalDistance = 0f
                                        currentDistance[0] = 0f
                                        progress = 1f
                                        updateCounter = 7
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val locationRequest = createLocationRequest()
        if (locPermissionState.status.shouldShowRationale) {
            showRationale = true
        } else if (locPermissionState.status.isGranted) {
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


    LaunchedEffect(key1 = currentLocation) {
        if (currentLocation != null) {
            locationName = mainViewModel.getLocationName(
                currentLocation!!.latitude,
                currentLocation!!.longitude,
                context
            )
            isMatched = locationName == dataToBeMatched
        }
        if (currentLocation != null && startPoint == null) {
            startPoint = currentLocation
        }
    }


    LaunchedEffect(key1 = isMatched) {
        isMatched?.let {
            if (it) {
                if (mainViewModel.isRealAlarm || previewMode) {
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
                                codeId = singleMission.codeId,
                                locId = singleMission.locId,
                                valuesToPick = singleMission.valuesToPick
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

                            "RangeNumbers" -> {
                                controller.navigate(Routes.RangeMemoryMissionPreview.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "RangeAlphabet" -> {
                                controller.navigate(Routes.RangeAlphabetMissionPreview.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "WalkOff" -> {
                                controller.navigate(Routes.WalkOffScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "ReachDestination" -> {
                                controller.navigate(Routes.AtLocationMissionScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "ArrangeNumbers" -> {
                                controller.navigate(Routes.ArrangeNumbersScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "ArrangeAlphabet" -> {
                                controller.navigate(Routes.ArrangeAlphabetsScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }

                            "ArrangeShapes" -> {
                                controller.navigate(Routes.ArrangeShapesScreen.route) {
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
                    mainViewModel.updateMyCurrentLocationToNull()
                    controller.navigate(Routes.ReachLocationMissionScreen.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop
                    }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = {
                        animatedProgress
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    color = Color.White,
                    trackColor = backColor,
                )

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 10.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )  {
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
                if (mainViewModel.isRealAlarm || previewMode) {
                    CustomButton(
                        onClick = {
                            controller.navigate(Routes.AlternativeMissionScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        text = "Emergency",
                        width = 0.36f, height = 39.dp,
                        backgroundColor = Color.Transparent,
                        isBorderPreview = true,
                        borderColor = Color.Red,
                        textColor = Color.Red, fontSize = 16.sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (loaderState) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Dialog(onDismissRequest = { /*TODO*/ }) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                if (!loaderState && currentLocation != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            uiSettings = uiSettings.copy(
                                scrollGesturesEnabled = true,
                                zoomControlsEnabled = true,
                                mapToolbarEnabled = true,
                                tiltGesturesEnabled = true,
                            ),
                            properties = mapProperties.copy(isMyLocationEnabled = true),
                            cameraPositionState = cameraPositionState,
                        ) {
                            calculatedLocation?.let { cal ->
                                Marker(
                                    state = MarkerState(
                                        position = LatLng(
                                            cal.latitude,
                                            cal.longitude
                                        )
                                    ),
                                    title = "Tapped Location", icon = bitmapDescriptorFromVector()
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "1. Reach the green marker wait for a while to complete the mission\n2. To start the mission,your current location needs to be fetched !",
                        color = Color.White,
                        fontSize = 23.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    CustomButton(onClick = {
                        if (!locationEnabled(context)) {
                            showDialog = true
                        } else {
                            mainViewModel.startLocationUpdates(fusedLocationClient)
                        }
                    }, text = "Get my location")
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "Location Services Disabled",
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                text = {
                    Text(
                        "Please enable location services and try again.",
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Okay", color = MaterialTheme.colorScheme.surfaceTint)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.surfaceTint)
                    }
                }
            )
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

private fun locationEnabled(context: Context): Boolean {
    val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}

private fun createLocationRequest(): LocationRequest {
    return LocationRequest.Builder(100, 1000).build()
}