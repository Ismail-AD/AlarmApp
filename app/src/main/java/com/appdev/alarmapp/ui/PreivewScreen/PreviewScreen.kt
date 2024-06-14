package com.appdev.alarmapp.ui.PreivewScreen

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.Analysis.playText
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.getRepeatText
import com.appdev.alarmapp.utils.listOfIntervals
import com.appdev.alarmapp.utils.newAlarmHandler
import com.appdev.alarmapp.utils.whichMissionHandler
import com.appdev.alarmapp.utils.whichRangeMissionHandle
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import java.lang.Integer.max
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PreviewScreen(
    ringtoneRepository: RingtoneRepository,
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {

    var filename by remember {
        mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.labelTextForSpeech else mainViewModel.newAlarm.labelTextForSpeech)
    }
    var switchStateLabel by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.isLabel else mainViewModel.newAlarm.isLabel) }
    var startItNow by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var loadingLabel by remember { mutableStateOf(true) }
    var playing by remember { mutableStateOf(false) }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = filename,
                selection = TextRange(filename.length)
            )
        )
    }

    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val alarmIdRec by mainViewModel.alarmID.collectAsState()
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val scrollState = rememberScrollState()
    val scrollStateRow = rememberScrollState()
    var switchState by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.willVibrate else mainViewModel.newAlarm.willVibrate) }
    var showRemaining by remember { mutableStateOf(false) }
    var currentState by remember { mutableStateOf(billingState.value) }
    var loading by remember { mutableStateOf(true) }
    var soundVolume by remember { mutableFloatStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.customVolume else mainViewModel.newAlarm.customVolume) }
    var showRepeatSheet by remember {
        mutableStateOf(false)
    }
    var showSnoozeSheet by remember {
        mutableStateOf(false)
    }
    var showLabel by remember {
        mutableStateOf(false)
    }
    var switchStateSnooze by remember { mutableStateOf(if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime != -1) true else if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime == -1) false else if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) true else !mainViewModel.whichAlarm.isOld && mainViewModel.newAlarm.snoozeTime != -1) }
    var selectedOptionSnooze by remember { mutableStateOf(if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime != -1) mainViewModel.defaultSettings.value.snoozeTime.toString() else if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) mainViewModel.selectedDataAlarm.snoozeTime.toString() else mainViewModel.newAlarm.snoozeTime.toString()) }
    val interactionSource = remember { MutableInteractionSource() }

    val sheetState = rememberModalBottomSheetState()
    val sheetStateSnooze = rememberModalBottomSheetState()
    val sheetStateLabel = rememberModalBottomSheetState()
    var getId by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    var days by rememberSaveable {
        mutableStateOf(
            getRepeatText(
                if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.listOfDays else mainViewModel.newAlarm.listOfDays
            )
        )
    }
    val alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, ringtoneRepository))
    }
    val selectedOptions =
        rememberSaveable { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.listOfDays else mainViewModel.newAlarm.listOfDays) }
    var remainingTime by remember { mutableStateOf(0L) }

    LaunchedEffect(key1 = showRemaining) {
        if (showRemaining) {
            Toast.makeText(
                context,
                "Alarm Rings in : ${formatTime(remainingTime)}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
        loading = false
        loadingLabel = false
    }
    LaunchedEffect(key1 = Unit) {
        if (mainViewModel.missionDetails.isSelected) {
            mainViewModel.missionData(
                MissionDataHandler.ResetData
            )
        }
    }
    LaunchedEffect(key1 = alarmIdRec, key2 = getId) {
        Log.d("CHKAI", "$alarmIdRec and ${mainViewModel.selectedDataAlarm.id} in LE")
        Log.d("CHKAI", "Button CLicked Status $getId in LE")
        if (mainViewModel.whichAlarm.isOld && getId) {
            mainViewModel.updateMissions(
                missionsEntity(
                    mainViewModel.selectedDataAlarm.id,
                    mainViewModel.missionDetailsList
                )
            )
            controller.navigate(Routes.MainScreen.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        } else {
            if (alarmIdRec != 0L && getId) {
                mainViewModel.insertMissions(
                    missionsEntity(
                        alarmIdRec,
                        mainViewModel.missionDetailsList
                    )
                )
                controller.navigate(Routes.MainScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit, key2 = soundVolume) {
        if (mainViewModel.whichAlarm.isOld) {
            if (mainViewModel.selectedDataAlarm.ringtone.name == "Silent") {
                soundVolume = 0f
            } else {
                soundVolume = mainViewModel.selectedDataAlarm.customVolume
            }
        } else {
            if (mainViewModel.newAlarm.ringtone.name == "Silent") {
                soundVolume = 0f
            } else {
                soundVolume = mainViewModel.newAlarm.customVolume
            }
        }


    }
    var timeToTrigger by remember {
        mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.localTime else mainViewModel.newAlarm.localTime)
    }



    DisposableEffect(textToSpeech) {
        onDispose {
            textToSpeech.stop()
        }
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "Cannot be used without adding a label !", Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
    }

    LaunchedEffect(key1 = textFieldValueState.text) {
        if (playing) {
            textToSpeech.stop()
            Helper.stopStream()
            playing = false
        }
        if (textFieldValueState.text.trim().isEmpty()) {
            switchStateLabel = false
        }
    }

    LaunchedEffect(key1 = startItNow) {
        if (startItNow) {
            Helper.playStream(context, R.raw.alarmsound)
        } else {
            Helper.stopStream()
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showLabel) {
        if (showLabel) {
            keyboardController?.show()
            focusRequester.requestFocus()
        } else
            keyboardController?.hide()
    }

    Scaffold(topBar = {
        TopBar(
            width = 0.9f,
            isDarkMode = isDarkMode,
            title = "Rings in ${
                timeInString(
                    calculateTimeUntil(
                        calculateAlarmTriggerTime(
                            AlarmEntity(
                                localTime = timeToTrigger, listOfDays = selectedOptions.value
                            )
                        )
                    )
                )
            }",
            actionText = ""
        ) {
            controller.navigate(Routes.MainScreen.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }, bottomBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            CustomButton(onClick = {
                if (mainViewModel.whichAlarm.isOld) {
                    if (selectedOptions.value.isEmpty()) {
                        mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(true))
                    } else {
                        mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(false))
                    }
                    if (mainViewModel.missionDetailsList.isEmpty()) {
                        mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = emptyList()))
                    } else {
                        mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = mainViewModel.missionDetailsList))
                    }
                    mainViewModel.updateHandler(EventHandlerAlarm.isActive(true))
                    mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = switchState))
                    mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = soundVolume))
                    Log.d("CHKZ", "Cancel before update : ${mainViewModel.selectedDataAlarm}")
                    alarmScheduler.cancel(mainViewModel.selectedDataAlarm)
                    scheduleTheAlarm(
                        mainViewModel,
                        mainViewModel.basicSettings.value.showInNotification,
                        mainViewModel.selectedDataAlarm,
                        alarmScheduler, mainViewModel.whichAlarm.isOld
                    ) { tomorrowTimeMillis, currentTimeMillis ->
                        remainingTime = tomorrowTimeMillis - currentTimeMillis
                    }
                    mainViewModel.updateHandler(EventHandlerAlarm.update)

                } else {

                    if (selectedOptions.value.isEmpty()) {
                        mainViewModel.newAlarmHandler(newAlarmHandler.isOneTime(true))
                    } else {
                        mainViewModel.newAlarmHandler(newAlarmHandler.isOneTime(false))
                    }
                    if (mainViewModel.missionDetailsList.isEmpty()) {

                        mainViewModel.newAlarmHandler(newAlarmHandler.getMissions(missions = emptyList()))
                    } else {
                        mainViewModel.newAlarmHandler(newAlarmHandler.getMissions(missions = mainViewModel.missionDetailsList))

                    }
                    mainViewModel.newAlarmHandler(newAlarmHandler.Vibrator(setVibration = switchState))
                    mainViewModel.newAlarmHandler(newAlarmHandler.CustomVolume(customVolume = soundVolume))
                    mainViewModel.newAlarmHandler(newAlarmHandler.isActive(true))
                    scheduleTheAlarm(
                        mainViewModel,
                        mainViewModel.basicSettings.value.showInNotification,
                        mainViewModel.newAlarm,
                        alarmScheduler,
                        mainViewModel.whichAlarm.isOld,
                    ) { tomorrowTimeMillis, currentTimeMillis ->
                        remainingTime = tomorrowTimeMillis - currentTimeMillis
                    }
                    mainViewModel.newAlarmHandler(newAlarmHandler.insert)

                }
                getId = true
                showRemaining = true
            }, text = "Save", width = 0.85f, backgroundColor = Color(0xff7B70FF))
        }
    }) {
        if (loading || loadingLabel) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                WheelTimePicker(
                    startTime = if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.localTime else mainViewModel.newAlarm.localTime,
                    timeFormat = TimeFormat.AM_PM,
                    size = DpSize(260.dp, 140.dp),
                    textColor = MaterialTheme.colorScheme.surfaceTint,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    selectorProperties = WheelPickerDefaults.selectorProperties(
                        enabled = true,
                        shape = RoundedCornerShape(5.dp),
                        color = if (isDarkMode) Color(0xFFf1faee).copy(alpha = 0.2f) else Color(
                            0xFFBDEAF5
                        ),
                        border = BorderStroke(2.dp, Color.Gray)
                    )
                ) { snappedTime ->
                    if (mainViewModel.whichAlarm.isOld) {
                        mainViewModel.updateHandler(EventHandlerAlarm.getTime(time = snappedTime))
                        timeToTrigger = snappedTime
                    } else {
                        mainViewModel.newAlarmHandler(newAlarmHandler.getTime(time = snappedTime))
                        timeToTrigger = snappedTime
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                SingleOption(
                    title = "Repeat",
                    data = days
                ) {
                    showRepeatSheet = true
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 15.dp, horizontal = 20.dp)
                ) {

                    Text(
                        text = "Mission",
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontSize = 16.sp
                    )
                    Text(
                        text = mainViewModel.missionDetailsList.size.toString() + "/5",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontSize = 15.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollStateRow)
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    // Calculate the number of empty static slots based on the mission list size

                    val emptyStaticSlots = max(5 - mainViewModel.missionDetailsList.size, 0)

                    // Display dynamic slots based on the list of missions
                    mainViewModel.missionDetailsList.forEach { mission ->
                        singleMission(isLock = false, mission, remove = { md ->
                            val newList = mainViewModel.missionDetailsList.toMutableList()
                            newList.remove(md)
                            mainViewModel.missionDetailsList = newList

                        }, moveToDetails = { misData ->
                            when (misData.missionName) {
                                "RangeNumbers" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.DifficultyLevel(
                                            misData.difficultyLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            misData.valuesToPick
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichRangeMissionHandler(
                                        whichRangeMissionHandle.thisMission(
                                            missionNumber = true,
                                            missionAlphabet = false,
                                            missionOrderAlphabet = false,
                                            missionOrderNumbers = false,
                                            missionOrderShapes = false
                                        )
                                    )
                                    controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "RangeAlphabet" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.DifficultyLevel(
                                            misData.difficultyLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            misData.valuesToPick
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichRangeMissionHandler(
                                        whichRangeMissionHandle.thisMission(
                                            missionNumber = false,
                                            missionAlphabet = true,
                                            missionOrderAlphabet = false,
                                            missionOrderNumbers = false,
                                            missionOrderShapes = false
                                        )
                                    )
                                    controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "ArrangeNumbers" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            misData.valuesToPick
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichRangeMissionHandler(
                                        whichRangeMissionHandle.thisMission(
                                            missionNumber = false,
                                            missionAlphabet = false,
                                            missionOrderAlphabet = false,
                                            missionOrderNumbers = true,
                                            missionOrderShapes = false
                                        )
                                    )
                                    controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                "ArrangeAlphabet" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            misData.valuesToPick
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichRangeMissionHandler(
                                        whichRangeMissionHandle.thisMission(
                                            missionNumber = false,
                                            missionAlphabet = false,
                                            missionOrderAlphabet = true,
                                            missionOrderNumbers = false,
                                            missionOrderShapes = false
                                        )
                                    )
                                    controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                "ArrangeShapes" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            misData.valuesToPick
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichRangeMissionHandler(
                                        whichRangeMissionHandle.thisMission(
                                            missionNumber = false,
                                            missionAlphabet = false,
                                            missionOrderAlphabet = true,
                                            missionOrderNumbers = false,
                                            missionOrderShapes = false
                                        )
                                    )
                                    controller.navigate(Routes.RangeMemoryMissionSetting.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                "ReachDestination" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.SelectedLocationID(
                                            misData.locId
                                        )
                                    )
                                    controller.navigate(Routes.ReachLocationMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "WalkOff" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = false,
                                            missionMath = false,
                                            missionShake = false,
                                            isSteps = false, isSquat = false, isWalk = true
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                "Memory" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = true,
                                            missionMath = false,
                                            missionShake = false,
                                            isSteps = false, isSquat = false, isWalk = false
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }


                                "Step" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = false,
                                            missionMath = false,
                                            missionShake = false,
                                            isSteps = true, isSquat = false, isWalk = false
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }


                                }

                                "Squat" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = false,
                                            missionMath = false,
                                            missionShake = false,
                                            isSteps = false, isSquat = true, isWalk = false
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }


                                }

                                "Shake" -> {

                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = false,
                                            missionMath = false,
                                            missionShake = true,
                                            isSteps = false, isSquat = false, isWalk = false
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                "Math" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.whichMissionHandle(
                                        whichMissionHandler.thisMission(
                                            missionMemory = false,
                                            missionMath = true,
                                            missionShake = false,
                                            isSteps = false, isSquat = false, isWalk = false
                                        )
                                    )
                                    controller.navigate(Routes.CommonMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "Typing" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionLevel(
                                            misData.missionLevel
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            misData.repeatTimes
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.SelectedSentences(
                                            convertStringToSet(misData.selectedSentences)
                                        )
                                    )
                                    controller.navigate(Routes.TypeMissionScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "Photo" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.ImageId(
                                            misData.imageId
                                        )
                                    )
                                    controller.navigate(Routes.CameraRoutineScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                "QR/Barcode" -> {
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionId(misData.missionID)
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.MissionName(
                                            misData.missionName
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            misData.isSelected
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.SelectedQrCode(
                                            misData.codeId
                                        )
                                    )
                                    controller.navigate(Routes.BarCodeDemoScreen.route) {
                                        popUpTo(Routes.Preview.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }

                                }

                                else -> {}
                            }
                        }) {
                            controller.navigate(Routes.MissionMenuScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }

                    // Display empty static slots
//                    if (currentState is BillingResultState.Success) {
                    repeat(emptyStaticSlots) {
                        singleMission(isLock = false,
                            Missions(),
                            remove = {},
                            moveToDetails = {}) {
                            controller.navigate(Routes.MissionMenuScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
//                    } else {
//                        if (mainViewModel.missionDetailsList.isEmpty()) {
//                            singleMission(isLock = false,
//                                Missions(),
//                                remove = {},
//                                moveToDetails = {}) {
//                                controller.navigate(Routes.MissionMenuScreen.route) {
//                                    launchSingleTop = true
//                                }
//                            }
//                        }
//                        repeat(emptyStaticSlots) {
//                            singleMission(isLock = true,
//                                Missions(),
//                                remove = {},
//                                moveToDetails = {}) {
//                                controller.navigate(Routes.Purchase.route) {
//                                    popUpTo(Routes.Preview.route) {
//                                        inclusive = false
//                                    }
//                                    launchSingleTop = true
//                                }
//                            }
//                        }
//                    }
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
            ) {
                Row(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 10.dp,
                        start = 18.dp,
                        end = 10.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeMute,
                        contentDescription = "", tint = MaterialTheme.colorScheme.surfaceTint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = soundVolume,
                        onValueChange = {
                            soundVolume =
                                if (mainViewModel.selectedDataAlarm.ringtone.name == "Silent" || mainViewModel.newAlarm.ringtone.name == "Silent") {
                                    0f
                                } else {
                                    it
                                }
                            if (mainViewModel.whichAlarm.isOld) {
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.CustomVolume(
                                        customVolume = it
                                    )
                                )
                            } else {
                                mainViewModel.newAlarmHandler(
                                    newAlarmHandler.CustomVolume(
                                        customVolume = it
                                    )
                                )

                            }
                        },
                        enabled = true,
                        valueRange = 0f..100f,
                        onValueChangeFinished = {},
                        steps = 0,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Gray.copy(alpha = 0.4f),
                            activeTrackColor = Color(0xff7358F5),
                            inactiveTrackColor = Color(0xff3C3F48)
                        ), modifier = Modifier.fillMaxWidth(0.8f), thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                modifier = Modifier.height(17.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Gray.copy(alpha = 0.4f),
                                    activeTrackColor = Color(0xff7358F5),
                                    inactiveTrackColor = Color(0xff3C3F48)
                                )
                            )
                        }, track = {
                            SliderDefaults.Track(
                                sliderState = it,
                                modifier = Modifier.height(10.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Gray.copy(alpha = 0.4f),
                                    activeTrackColor = Color(0xff7358F5),
                                    inactiveTrackColor = Color(0xff3C3F48)
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vibration",
                        color = MaterialTheme.colorScheme.surfaceTint,
                        textAlign = TextAlign.Start, fontSize = 16.sp
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Switch(
                            checked = switchState,
                            onCheckedChange = { newSwitchState ->
                                switchState = newSwitchState
                                // Handle the new switch state
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDarkMode) Color.White else Color(
                                    0xff13A7CB
                                ), // Color when switch is ON
                                checkedTrackColor = if (isDarkMode) Color(0xff7358F5) else Color(
                                    0xff7FCFE1
                                ), // Track color when switch is ON
                                uncheckedThumbColor = if (isDarkMode) Color(0xff949495) else Color(
                                    0xff656D7D
                                ), // Color when switch is OFF
                                uncheckedTrackColor = if (isDarkMode) Color(0xff343435) else Color(
                                    0xff9E9E9E
                                ) // Track color when switch is OFF
                            ), modifier = Modifier.scale(0.8f)
                        )
                    }
                }


                SingleOption(
                    title = "Sound",
                    data = if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.ringtone.name else mainViewModel.newAlarm.ringtone.name
                ) {
                    controller.navigate(Routes.Ringtone.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }

                SingleOption(
                    title = "Sound power-up",
                    data = if (mainViewModel.whichAlarm.isOld) getValue(
                        mainViewModel.selectedDataAlarm.isLoudEffect,
                        mainViewModel.selectedDataAlarm.isTimeReminder,
                        mainViewModel.selectedDataAlarm.isGentleWakeUp
                    ) else getValue(
                        mainViewModel.newAlarm.isLoudEffect,
                        mainViewModel.newAlarm.isTimeReminder,
                        mainViewModel.newAlarm.isGentleWakeUp
                    )
                ) {
                    controller.navigate(Routes.SoundPowerUpScreen.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true

                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
            ) {
                SingleOption(
                    title = "Snooze",
                    data = if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) "${mainViewModel.selectedDataAlarm.snoozeTime} min" else if (!mainViewModel.whichAlarm.isOld && mainViewModel.newAlarm.snoozeTime != -1) "${mainViewModel.newAlarm.snoozeTime} min" else "off"
                ) {
                    showSnoozeSheet = true
//                    controller.navigate(Routes.SnoozeScreen.route) {
//                        popUpTo(controller.graph.startDestinationId)
//                        launchSingleTop = true
//                    }
                }
                SingleOption(
                    title = "Label",
                    data = if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.labelTextForSpeech.trim()
                            .isNotEmpty()
                    ) mainViewModel.selectedDataAlarm.labelTextForSpeech else if (!mainViewModel.whichAlarm.isOld && mainViewModel.newAlarm.labelTextForSpeech.trim()
                            .isNotEmpty()
                    ) mainViewModel.newAlarm.labelTextForSpeech else "none"
                ) {
                    showLabel = true
//                    controller.navigate(Routes.LabelScreen.route) {
//                        popUpTo(controller.graph.startDestinationId)
//                        launchSingleTop = true
//                    }
                }
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 25.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                CustomButton(onClick = {
//                    if (mainViewModel.whichAlarm.isOld) {
//                        if (selectedOptions.value.isEmpty()) {
//                            mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(true))
//                        } else {
//                            mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(false))
//                        }
//                        if (mainViewModel.missionDetailsList.isEmpty()) {
//                            mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = emptyList()))
//                        } else {
//                            mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = mainViewModel.missionDetailsList))
//                        }
//                        mainViewModel.updateHandler(EventHandlerAlarm.isActive(true))
//                        mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = switchState))
//                        mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = soundVolume))
//                        Log.d("CHKZ", "Cancel before update : ${mainViewModel.selectedDataAlarm}")
//                        alarmScheduler.cancel(mainViewModel.selectedDataAlarm)
//                        scheduleTheAlarm(
//                            mainViewModel,
//                            mainViewModel.basicSettings.value.showInNotification,
//                            mainViewModel.selectedDataAlarm,
//                            alarmScheduler, mainViewModel.whichAlarm.isOld
//                        ) { tomorrowTimeMillis, currentTimeMillis ->
//                            remainingTime = tomorrowTimeMillis - currentTimeMillis
//                        }
//                        mainViewModel.updateHandler(EventHandlerAlarm.update)
//
//                    } else {
//
//                        if (selectedOptions.value.isEmpty()) {
//                            mainViewModel.newAlarmHandler(newAlarmHandler.isOneTime(true))
//                        } else {
//                            mainViewModel.newAlarmHandler(newAlarmHandler.isOneTime(false))
//                        }
//                        if (mainViewModel.missionDetailsList.isEmpty()) {
//
//                            mainViewModel.newAlarmHandler(newAlarmHandler.getMissions(missions = emptyList()))
//                        } else {
//                            mainViewModel.newAlarmHandler(newAlarmHandler.getMissions(missions = mainViewModel.missionDetailsList))
//
//                        }
//                        mainViewModel.newAlarmHandler(newAlarmHandler.Vibrator(setVibration = switchState))
//                        mainViewModel.newAlarmHandler(newAlarmHandler.CustomVolume(customVolume = soundVolume))
//                        mainViewModel.newAlarmHandler(newAlarmHandler.isActive(true))
//                        scheduleTheAlarm(
//                            mainViewModel,
//                            mainViewModel.basicSettings.value.showInNotification,
//                            mainViewModel.newAlarm,
//                            alarmScheduler,
//                            mainViewModel.whichAlarm.isOld,
//                        ) { tomorrowTimeMillis, currentTimeMillis ->
//                            remainingTime = tomorrowTimeMillis - currentTimeMillis
//                        }
//                        mainViewModel.newAlarmHandler(newAlarmHandler.insert)
//
//                    }
//                    getId = true
//                    showRemaining = true
//                }, text = "Save", width = 0.85f, backgroundColor = Color(0xff7B70FF))
//            }
        }
        if (showRepeatSheet) {

            ModalBottomSheet(onDismissRequest = {
                selectedOptions.value =
                    if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.listOfDays else mainViewModel.newAlarm.listOfDays
                showRepeatSheet = false
            }, sheetState = sheetState) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onBackground)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, bottom = 5.dp, end = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repeat",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                        )
                    }
                    // Replace this with your logic to get the list of days
                    val daysOfWeek = listOf(
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday",
                        "Sunday"
                    )

                    val dayPairs = daysOfWeek.chunked(2)

                    dayPairs.forEach { pair ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            pair.forEach { day ->
                                // Checkbox for each day
                                Checkbox(
                                    checked = selectedOptions.value.contains(day),
                                    onCheckedChange = {
                                        selectedOptions.value = if (it) {
                                            selectedOptions.value + day
                                        } else {
                                            selectedOptions.value - day
                                        }
                                    }, colors = CheckboxDefaults.colors(
                                        uncheckedColor = Color(0xffB6BDCA),
                                        checkmarkColor = Color.White,
                                        checkedColor = Color(0xff18677E)
                                    ),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .align(Alignment.CenterVertically)
                                )

                                // Display day of the week
                                Text(
                                    text = day,
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    CustomButton(
                        onClick = {
                            if (mainViewModel.whichAlarm.isOld) {
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.getDays(
                                        days = selectedOptions.value
                                    )
                                )
                            } else {
                                mainViewModel.newAlarmHandler(
                                    newAlarmHandler.getDays(
                                        days = selectedOptions.value
                                    )
                                )
                            }
                            days = getRepeatText(selectedOptions.value)
                            showRepeatSheet = false
                        },
                        text = "Done",
                        width = 0.98f
                    )
                }
            }
        }
        if (showSnoozeSheet) {
            ModalBottomSheet(onDismissRequest = {
                if (mainViewModel.managingDefault) {
                    if (!switchStateSnooze) {
                        mainViewModel.setDefaultSettings(
                            DefaultSettingsHandler.GetNewObject(
                                defaultSettings = DefaultSettings(
                                    id = mainViewModel.defaultSettings.value.id,
                                    ringtone = mainViewModel.defaultSettings.value.ringtone,
                                    snoozeTime = -1,
                                    listOfMissions = mainViewModel.defaultSettings.value.listOfMissions
                                )
                            )
                        )
                    } else {
                        mainViewModel.setDefaultSettings(
                            DefaultSettingsHandler.GetNewObject(
                                defaultSettings = DefaultSettings(
                                    id = mainViewModel.defaultSettings.value.id,
                                    ringtone = mainViewModel.defaultSettings.value.ringtone,
                                    snoozeTime = selectedOptionSnooze.toInt(),
                                    listOfMissions = mainViewModel.defaultSettings.value.listOfMissions
                                )
                            )
                        )
                    }
                    mainViewModel.setDefaultSettings(DefaultSettingsHandler.UpdateDefault)
                } else {
                    if (mainViewModel.whichAlarm.isOld && switchStateSnooze) {
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = selectedOptionSnooze.toInt()))
                    } else if (!mainViewModel.whichAlarm.isOld && switchStateSnooze) {
                        mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = selectedOptionSnooze.toInt()))
                    } else if (mainViewModel.whichAlarm.isOld && !switchStateSnooze) {
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = -1))
                    } else {
                        mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = -1))
                    }
                }
                showSnoozeSheet = false
            }, sheetState = sheetStateSnooze) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onBackground)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Snooze",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = switchStateSnooze,
                            onCheckedChange = { newSwitchState ->
                                switchStateSnooze = newSwitchState

                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isSystemInDarkTheme()) Color.White else Color(
                                    0xff13A7CB
                                ), // Color when switch is ON
                                checkedTrackColor = if (isSystemInDarkTheme()) Color(0xff7358F5) else Color(
                                    0xff7FCFE1
                                ), // Track color when switch is ON
                                uncheckedThumbColor = if (isSystemInDarkTheme()) Color(0xff949495) else Color(
                                    0xff656D7D
                                ), // Color when switch is OFF
                                uncheckedTrackColor = if (isSystemInDarkTheme()) Color(0xff343435) else Color(
                                    0xff9E9E9E
                                ) // Track color when switch is OFF
                            ), modifier = Modifier.scale(0.8f)
                        )
                    }
                    val groupedIntervals =
                        listOfIntervals.chunked(2) // Split intervals into chunks of size 2

                    groupedIntervals.forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            chunk.forEach { interval ->
                                RadioButton(
                                    selected = interval == selectedOptionSnooze,
                                    onClick = { selectedOptionSnooze = interval },
                                    enabled = switchStateSnooze,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xff18677E),
                                        unselectedColor = Color(0xffB6BDCA)
                                    )
                                )
                                Text(
                                    text = if (interval != "Off") "$interval minutes" else interval,
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 15.sp,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }
//                    listOfIntervals.forEach { item ->
//                        SingleChoice(
//                            isSelected = item == selectedOptionSnooze,
//                            onCLick = { selectedOptionSnooze = item },
//                            title = item, enabled = switchStateSnooze
//                        )
//                    }
//                    Card(
//                        onClick = {},
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp, vertical = 20.dp),
//                        enabled = switchStateSnooze,
//                        shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.onBackground,
//                            disabledContainerColor = Color(0xff16171B)
//                        )
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                        ) {
//                            listOfIntervals.forEach { item ->
//                                Spacer(modifier = Modifier.height(12.dp))
//                                SingleChoice(
//                                    isSelected = item == selectedOptionSnooze,
//                                    onCLick = { selectedOptionSnooze = item },
//                                    title = item, enabled = switchStateSnooze
//                                )
//                            }
//
//                        }
//
//                    }

                }
            }
        }

        if (showLabel) {
            ModalBottomSheet(onDismissRequest = {
//                if (currentState is BillingResultState.Success) {
                if (mainViewModel.whichAlarm.isOld) {
                    mainViewModel.updateHandler(
                        EventHandlerAlarm.IsLabel(
                            isLabelOrNot = switchStateLabel
                        )
                    )
                    mainViewModel.updateHandler(
                        EventHandlerAlarm.LabelText(
                            getLabelText = textFieldValueState.text
                        )
                    )
                } else {
                    mainViewModel.newAlarmHandler(
                        newAlarmHandler.IsLabel(
                            isLabelOrNot = switchStateLabel
                        )
                    )
                    mainViewModel.newAlarmHandler(
                        newAlarmHandler.LabelText(
                            getLabelText = textFieldValueState.text
                        )
                    )
                }
//                } else {
//                    filename = ""
//                    textFieldValueState = TextFieldValue(
//                        text = filename,
//                        selection = TextRange(filename.length)
//                    )
//                }
                playing = false
                showLabel = false
            }, sheetState = sheetStateLabel) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(horizontal = 15.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicTextField(
                        value = textFieldValueState,
                        onValueChange = {
                            textFieldValueState = it
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(
                                    start = 3.dp,
                                    end = 2.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 30.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp, horizontal = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reads out the label when your alarm rings",
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Start,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Card(
                                onClick = {
                                    playing = !playing
                                    if (playing) {
                                        playText(
                                            textToSpeech, text = textFieldValueState.text,
                                            id = System.currentTimeMillis().toString()
                                        ) { endOrNot ->
                                            startItNow = endOrNot
                                        }
                                    } else {
                                        startItNow = false
                                        textToSpeech.stop()
                                    }
                                },
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(85.dp),
                                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (textFieldValueState.text.trim()
                                            .isEmpty()
                                    ) Color(0x434C5460) else MaterialTheme.colorScheme.surfaceTint
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (textFieldValueState.text.trim()
                                            .isEmpty()
                                    ) Color(
                                        0x434C5460
                                    ) else Color.Transparent,
                                    disabledContainerColor = if (textFieldValueState.text.trim()
                                            .isEmpty()
                                    ) Color(
                                        0x434C5460
                                    ) else Color.Transparent
                                ), enabled = textFieldValueState.text.trim()
                                    .isNotEmpty()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.surfaceTint,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "Preview", fontSize = 13.sp,
                                        letterSpacing = 0.sp,
                                        color = MaterialTheme.colorScheme.surfaceTint
                                    )
                                }
                            }

                        }
                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Label Reminder",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp,
                            )
                            if (currentState !is BillingResultState.Success) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.surfaceTint,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Switch(
                                checked = switchStateLabel,
                                onCheckedChange = { newSwitchState ->
                                    if (textFieldValueState.text.trim().isEmpty()) {
                                        showToast = true
                                    } else {
//                                        if (currentState !is BillingResultState.Success) {
//                                            controller.navigate(Routes.Purchase.route) {
//                                                popUpTo(Routes.LabelScreen.route) {
//                                                    inclusive = false
//                                                }
//                                                launchSingleTop = true
//                                            }
//                                        } else {
                                        switchStateLabel = newSwitchState
//                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isDarkMode) Color.White else Color(
                                        0xff13A7CB
                                    ), // Color when switch is ON
                                    checkedTrackColor = if (isDarkMode) Color(0xff7358F5) else Color(
                                        0xff7FCFE1
                                    ), // Track color when switch is ON
                                    uncheckedThumbColor = if (isDarkMode) Color(0xff949495) else Color(
                                        0xff656D7D
                                    ), // Color when switch is OFF
                                    uncheckedTrackColor = if (isDarkMode) Color(0xff343435) else Color(
                                        0xff9E9E9E
                                    ) // Track color when switch is OFF
                                ), modifier = Modifier.scale(0.8f)
                            )
                        }

                    }
                }
            }
        }
    }
}

fun getValue(loudEffect: Boolean, timeReminder: Boolean, gentleWakeUp: Boolean): String {
    // Count the number of true values
    val trueCount = listOf(loudEffect, timeReminder, gentleWakeUp).count { it }

    // Return the corresponding value based on the number of true values
    return when (trueCount) {
        3 -> "3 in use"
        2 -> "2 in use"
        1 -> "1 in use"
        else -> "off"
    }
}

fun getLabelForSliderValue(value: Float): String {
    return when (value.roundToInt()) {
        0 -> "Very Easy"
        1 -> "Easy"
        2 -> "Normal"
        3 -> "Hard"
        else -> ""
    }
}

fun getImageForSliderValue(value: String): Int {
    return when (value) {
        "Very Easy" -> R.drawable.easy
        "Easy" -> R.drawable.medium
        "Normal" -> R.drawable.normal
        "Hard" -> R.drawable.hardp
        else -> R.drawable.easy
    }
}

fun getImageForSelectedValue(value: String): Int {
    return when (value) {
        "Normal Mode" -> R.drawable.normalnum
        "Hard Mode" -> R.drawable.hardnum
        else -> R.drawable.normalnum
    }
}

fun getAlphaImageForSelectedValue(value: String): Int {
    return when (value) {
        "Normal Mode" -> R.drawable.normalalpha
        "Hard Mode" -> R.drawable.hardalpha
        else -> R.drawable.normalalpha
    }
}


fun getMathEqForSliderValue(value: String): String {
    return when (value) {
        "Very Easy" -> "3 + 4 = "
        "Easy" -> "16 + 25  = "
        "Normal" -> "43 + 32 + 34 = "
        "Hard" -> " (6 x 5) + 20 = "
        else -> "3 + 4 = "
    }
}
//@Composable
//fun SingleChoiceSnooze(isSelected: Boolean, onCLick: () -> Unit, title: String, enabled: Boolean) {
//        RadioButton(selected = isSelected, onClick = { onCLick() }, enabled = enabled, colors = RadioButtonDefaults.colors(selectedColor = Color(0xff18677E), unselectedColor = Color(0xffB6BDCA)))
//        Text(
//            text = if(title!="Off") "$title minutes" else title,
//            color = MaterialTheme.colorScheme.surfaceTint,
//            fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp).weight(1f)
//        )
//}

fun calculateSkipAlarmTriggerTime(alarmEntity: AlarmEntity): Long {
    val newTime = localTimeToMillis(alarmEntity.localTime)
    Log.d("CHKITNOW", "${Date(newTime)} new date to trigger")
    if (alarmEntity.listOfDays.isNotEmpty()) {

        val calendar2 = Calendar.getInstance()
        val date2 = Date(newTime)
        Log.d("CHKITNOW", "${date2} date to trigger")
        calendar2.time = date2

        // Get the day of the week from the calendar
        var dayOfWeekNew = calendar2.get(Calendar.DAY_OF_WEEK)


        val selectedMillis = alarmEntity.nextTimeInMillis
        val calendarNew = Calendar.getInstance()
        calendarNew.timeInMillis = selectedMillis

// Get the day of the week from the calendar
        var dayOfTrigger = calendarNew.get(Calendar.DAY_OF_WEEK)

        Log.d("CHKITNOW", "${dayOfTrigger} day of trigger")

// Calculate the difference in milliseconds between the day of trigger and the current day
        val remaining = dayOfTrigger.toLong() - dayOfWeekNew
        val dayDifferenceMillis = if (remaining == 0L) {
            TimeUnit.DAYS.toMillis(7)
        } else {
            TimeUnit.DAYS.toMillis(remaining)
        }

        Log.d("CHKITNOW", "${dayDifferenceMillis} CALCULATED DAY... to trigger")
// Calculate the adjusted selected time by adding the day difference and the new time
        val selectedTimeMillis = dayDifferenceMillis + newTime

        Log.d("CHKITNOW", "${Date(selectedTimeMillis)} CALCULATED date to trigger")
        val calendar = Calendar.getInstance()
        val date = Date(selectedTimeMillis)
        Log.d("CHKITNOW", "${date} date to trigger")
        calendar.time = date

        // Get the day of the week from the calendar
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        Log.d("CHKITNOW", "${dayOfWeek} day to trigger")

        val nextOccurrence = alarmEntity.listOfDays
            .map { com.appdev.alarmapp.AlarmManagement.getDayOfWeek(it) }
            .filter { it >= dayOfWeek }
            .minOrNull() ?: alarmEntity.listOfDays
            .map { com.appdev.alarmapp.AlarmManagement.getDayOfWeek(it) }
            .minOrNull()!!
        val daysUntilNextOccurrence =
            nextOccurrence - dayOfWeek
        val triggerTimeMillis = when {
            daysUntilNextOccurrence < 0 -> {
                selectedTimeMillis + TimeUnit.DAYS.toMillis(
                    (7 - kotlin.math.abs(
                        daysUntilNextOccurrence
                    )).toLong()
                )
            }

            selectedTimeMillis <= System.currentTimeMillis() && daysUntilNextOccurrence == 0 -> {
                if (alarmEntity.listOfDays.size == 1) {
                    selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
                } else {
                    val nextOccurrenceAgain = alarmEntity.listOfDays
                        .map { getDayOfWeek(it) }
                        .filter { it > dayOfWeek }
                        .minOrNull() ?: alarmEntity.listOfDays
                        .map { getDayOfWeek(it) }
                        .minOrNull()!!

                    val daysUntilNextOccurrenceAgain = nextOccurrenceAgain - dayOfWeek

                    if (daysUntilNextOccurrenceAgain < 0) {
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(
                            (7 - kotlin.math.abs(
                                daysUntilNextOccurrenceAgain
                            )).toLong()
                        )
                    } else {
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrenceAgain.toLong())
                    }
                }
            }

            else -> {
                selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
            }
        }
        return triggerTimeMillis
    } else {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = newTime
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}


fun calculateAlarmTriggerTime(alarmEntity: AlarmEntity): Long {
    val selectedTimeMillis = localTimeToMillis(alarmEntity.localTime)

    if (alarmEntity.listOfDays.isNotEmpty()) {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)


        val nextOccurrence = alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .filter { it >= currentDayOfWeek }
            .minOrNull() ?: alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .minOrNull()!!

        Log.d("CHKW", "At preview SELECTED on : ${convertMillisToLocalTime(selectedTimeMillis)} ")
        Log.d("CHKW", "At preview occurence : ${nextOccurrence} ")
        Log.d("CHKW", "DATD FROM FILTER : ${
            alarmEntity.listOfDays
                .map { getDayOfWeek(it) }
                .filter { it >= currentDayOfWeek }
                .minOrNull()
        } ")


        val daysUntilNextOccurrence = nextOccurrence - currentDayOfWeek
        Log.d("CHKW", "Left days : ${daysUntilNextOccurrence} ")

        val triggerTimeMillis = when {
            daysUntilNextOccurrence < 0 -> {
                selectedTimeMillis + TimeUnit.DAYS.toMillis(
                    (7 - kotlin.math.abs(
                        daysUntilNextOccurrence
                    )).toLong()
                )
            }

            selectedTimeMillis <= System.currentTimeMillis() && daysUntilNextOccurrence == 0 -> {
                if (alarmEntity.listOfDays.size == 1) {
                    selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
                } else {
                    val nextOccurrenceAgain = alarmEntity.listOfDays
                        .map { getDayOfWeek(it) }
                        .filter { it > currentDayOfWeek }
                        .minOrNull() ?: alarmEntity.listOfDays
                        .map { getDayOfWeek(it) }
                        .minOrNull()!!

                    val daysUntilNextOccurrenceAgain = nextOccurrenceAgain - currentDayOfWeek

                    if (daysUntilNextOccurrenceAgain < 0) {
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(
                            (7 - kotlin.math.abs(
                                daysUntilNextOccurrenceAgain
                            )).toLong()
                        )
                    } else {
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrenceAgain.toLong())
                    }
                }
            }

            else -> {
                selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
            }
        }

        return triggerTimeMillis
    } else {
        if (selectedTimeMillis <= System.currentTimeMillis() && alarmEntity.listOfDays.isEmpty()) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedTimeMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }

            return calendar.timeInMillis
        } else {
            return selectedTimeMillis
        }
    }
}


fun scheduleTheAlarm(
    mainViewModel: MainViewModel,
    notify: Boolean,
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
    isOld: Boolean,
    updateRemainingTime: (Long, Long) -> Unit
) {
    val triggerTimeMillis = if (alarmEntity.skipTheAlarm) {
        calculateSkipAlarmTriggerTime(alarmEntity)
    } else {
        calculateAlarmTriggerTime(alarmEntity)
    }

    Log.d("CHKTM", "At preview on : ${convertMillisToLocalTime(triggerTimeMillis)} ")
    Log.d("CHKTM", "At preview upcome : ${convertMillisToLocalTime(triggerTimeMillis)} ")
    if (!isOld) {
        alarmEntity.id = triggerTimeMillis + (0..19992).random()
        mainViewModel.newAlarmHandler(newAlarmHandler.getMilli(timeInMilli = triggerTimeMillis))
        mainViewModel.newAlarmHandler(newAlarmHandler.getNextMilli(upcomingMilli = triggerTimeMillis))
    } else {
        if (alarmEntity.skipTheAlarm) {
            mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarmEntity.timeInMillis))
            mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = triggerTimeMillis))
        } else {
            mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = triggerTimeMillis))
            mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = triggerTimeMillis))
        }
    }

    alarmEntity.timeInMillis = triggerTimeMillis
    alarmEntity.nextTimeInMillis = triggerTimeMillis

    val instant = Instant.ofEpochMilli(triggerTimeMillis)
    val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
    alarmEntity.localTime = offsetTime.toLocalTime()


    alarmScheduler.schedule(alarmEntity, notify)

    updateRemainingTime(alarmEntity.timeInMillis, System.currentTimeMillis())
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun singleMission(
    isLock: Boolean,
    missionData: Missions,
    remove: (Missions) -> Unit,
    moveToDetails: (Missions) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = {
            if (missionData.missionID < 1) {
                onClick()
            } else {
                moveToDetails(missionData)
            }
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(85.dp)
            .width(85.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = if (missionData.isSelected) BorderStroke(
            width = 0.dp,
            color = Color.Transparent
        ) else BorderStroke(width = 2.dp, color = if (isLock) Color(0xffA6ACB5) else signatureBlue)
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxSize(), contentAlignment = Alignment.Center
        ) {

            if (missionData.isSelected) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFD5E6FF)),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(), contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = { remove(missionData) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "",
                                tint = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val IconID = when (missionData.missionName) {
                        "Memory" -> Icons.Filled.AutoAwesomeMosaic
                        "Math" -> Icons.Filled.Calculate
                        "Shake" -> Icons.Filled.ScreenRotation
                        "Typing" -> Icons.Filled.Keyboard
                        "Photo" -> Icons.Filled.CameraEnhance
                        "QR/Barcode" -> Icons.Filled.QrCode2
                        "ArrangeNumbers" -> Icons.Filled.RepeatOne
                        "ArrangeAlphabet" -> Icons.AutoMirrored.Filled.CompareArrows
                        "ArrangeShapes" -> Icons.Filled.Pentagon
                        "RangeNumbers" -> Icons.Filled.LooksOne
                        "RangeAlphabet" -> Icons.Filled.SortByAlpha
                        "ReachDestination" -> Icons.Filled.AddLocationAlt
                        "WalkOff" -> Icons.AutoMirrored.Filled.DirectionsWalk
                        else -> {
                            null
                        }
                    }
                    if (IconID != null) {
                        Icon(
                            imageVector = IconID,
                            contentDescription = "",
                            tint = Color.DarkGray
                        )
                    } else {
                        val drawableId =
                            if (missionData.missionName == "Step") R.drawable.stepsblack else R.drawable.strengthblack
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp)
                        )

                    }
                    Text(
                        text = "${missionData.repeatTimes} ${if (missionData.missionName == "WalkOff") "meters" else if (missionData.missionName == "Step") "Steps" else "times"}",
                        fontSize = 12.sp, color = Color.DarkGray
                    )
                }
            } else {
                Icon(
                    imageVector = if (!isLock) Icons.Filled.Add else Icons.Outlined.Lock,
                    contentDescription = "",
                    tint = Color(0xffA6ACB5)
                )
            }
        }
    }
}

@Composable
fun SingleOption(
    title: String,
    data: String,
    isLock: Boolean = false,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.surfaceTint,
            textAlign = TextAlign.Start, fontSize = 16.sp
        )
        if (isLock) {
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.size(15.dp)
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    onClick()
                }
            ) {
                Text(
                    if (data.length > 10) {
                        data.take(10) + "..."
                    } else {
                        data
                    }, fontSize = 15.sp,
                    letterSpacing = 0.sp,
                    color = signatureBlue
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isDarkMode: Boolean,
    width: Float = 0f,
    title: String,
    actionText: String = "",
    backColor: Color = MaterialTheme.colorScheme.background,
    onClick: () -> Unit,
) {
    TopAppBar(title = {
        if (width != 0f) {
            Box(modifier = Modifier.fillMaxWidth(width), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.surfaceTint, fontSize = 16.sp
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.surfaceTint, fontSize = 16.sp
                )
            }
        }
    }, navigationIcon = {
        IconButton(onClick = { onClick() }) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIos,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.size(23.dp)
            )
        }
    }, actions = {
        Text(text = actionText, color = MaterialTheme.colorScheme.surfaceTint, fontSize = 14.sp)
    }, colors = topAppBarColors(
        containerColor = if (isDarkMode) backColor else MaterialTheme.colorScheme.background,
        navigationIconContentColor = Color.White
    )
    )
}

private fun getDayOfWeek(day: String): Int {
    val daysOfWeek =
        listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    return daysOfWeek.indexOf(day.lowercase(Locale.ROOT)) + 1
}

@RequiresApi(Build.VERSION_CODES.O)
fun localTimeToMillis(localTime: LocalTime): Long {
    val currentDate = LocalDateTime.now()
    val dateTimeToSchedule = LocalDateTime.of(currentDate.toLocalDate(), localTime)
    return dateTimeToSchedule.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun formatTime(remainingTimeMillis: Long): String {
    if (remainingTimeMillis < TimeUnit.SECONDS.toMillis(1)) {
        return "Less than a minute"
    }

    val days = TimeUnit.MILLISECONDS.toDays(remainingTimeMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(remainingTimeMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60

    val formattedTime = StringBuilder()
    if (days > 0) {
        formattedTime.append("$days day${if (days > 1) "s" else ""} ")
    }
    if (hours > 0) {
        formattedTime.append("$hours hour${if (hours > 1) "s" else ""} ")
    }
    if (minutes > 0) {
        formattedTime.append("$minutes minute${if (minutes > 1) "s" else ""} ")
    }
    if (seconds > 0 || formattedTime.isEmpty()) {
        formattedTime.append("$seconds second${if (seconds > 1) "s" else ""}")
    }

    return formattedTime.toString().trim()
}

@Composable
fun MissionSelection(iconID: ImageVector, title: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconID,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
            )
        }
    }
}

data class TimeUntil(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

fun calculateTimeUntil(timestamp: Long): TimeUntil {
    val currentTime = System.currentTimeMillis()
    val timeDifference = timestamp - currentTime
    val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
    val hours = TimeUnit.MILLISECONDS.toHours(timeDifference) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference) % 60
    return TimeUntil(days, hours, minutes, seconds)
}

fun pluralize(value: Long, unit: String): String {
    return if (value > 1) "$value ${unit}s" else "$value $unit"
}

fun timeInString(timeToTrigger: TimeUntil): String {
    return when {
        timeToTrigger.days > 0 ->
            "${pluralize(timeToTrigger.days, "day")} ${pluralize(timeToTrigger.hours, "hr")}"

        timeToTrigger.hours > 0 ->
            "${pluralize(timeToTrigger.hours, "hr")} ${pluralize(timeToTrigger.minutes, "min")}"

        timeToTrigger.minutes > 0 ->
            "${pluralize(timeToTrigger.minutes, "min")} ${pluralize(timeToTrigger.seconds, "sec")}"

        else -> pluralize(timeToTrigger.seconds, "sec")
    }
}

fun playText(textToSpeech: TextToSpeech, id: String, text: String, startItNow: (Boolean) -> Unit) {
    textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
        }

        override fun onDone(utteranceId: String?) {
            startItNow(true)
        }

        override fun onError(utteranceId: String?) {
        }
    })

    val params = Bundle()
    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)

    // Play the formatted time and date as audio
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
}