package com.appdev.alarmapp.ui.PreivewScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.andyliu.compose_wheel_picker.VerticalWheelPicker
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.getRepeatText
import com.appdev.alarmapp.utils.newAlarmHandler
import com.appdev.alarmapp.utils.weekDays
import com.appdev.alarmapp.utils.whichMissionHandler
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Math.abs
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.reflect.KFunction1


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PreviewScreen(
    controller: NavHostController,
    mainViewModel: MainViewModel
) {

    if (Helper.isPlaying()) {
        Helper.stopStream()
    }

    val scrollState = rememberScrollState()
    val scrollStateRow = rememberScrollState()
    val scrollStateCol = rememberScrollState()
    var switchState by remember { mutableStateOf(false) }
    var showRemaining by remember { mutableStateOf(false) }
    var soundVolume by remember { mutableFloatStateOf(0f) }
    var choiceSliderState by remember { mutableFloatStateOf(0f) }
    var showRepeatSheet by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val tokenManagement by remember {
        mutableStateOf(TokenManagement(context))
    }

    var showMissionSheet by remember {
        mutableStateOf(false)
    }
    var showSetMissionSheet by remember {
        mutableStateOf(false)
    }
    var showMathSheet by remember {
        mutableStateOf(false)
    }
    var showShakeSheet by remember {
        mutableStateOf(false)
    }
    var coroutine = rememberCoroutineScope()

    var days by rememberSaveable {
        mutableStateOf(
            getRepeatText(
                if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.listOfDays else mainViewModel.newAlarm.listOfDays
            )
        )
    }


    val alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, mainViewModel))
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

    Scaffold(topBar = {
        TopBar(title = "Ring in 2 days", actionText = "Preview") {
            controller.navigate(Routes.MainScreen.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(it)
                .background(backColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(elementBack),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WheelTimePicker(
                    startTime = if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.localTime else mainViewModel.newAlarm.localTime,
                    timeFormat = TimeFormat.AM_PM,
                    size = DpSize(260.dp, 140.dp),
                    textColor = Color.White,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    selectorProperties = WheelPickerDefaults.selectorProperties(
                        enabled = true,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFFf1faee).copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, Color.Gray)
                    ), modifier = Modifier.padding(top = 22.dp)
                ) { snappedTime ->
                    if (mainViewModel.whichAlarm.isOld) {
                        mainViewModel.updateHandler(EventHandlerAlarm.getTime(time = snappedTime))
                    } else {
                        mainViewModel.newAlarmHandler(newAlarmHandler.getTime(time = snappedTime))
                    }
                }
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
                    .padding(top = 8.dp)
                    .background(elementBack), verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 33.dp, horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "Mission",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = mainViewModel.missionDetailsList.size.toString() + "/5",
                            color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollStateRow)
                            .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        // Calculate the number of empty static slots based on the mission list size
                        val emptyStaticSlots = max(5 - mainViewModel.missionDetailsList.size, 0)

                        // Display dynamic slots based on the list of missions
                        mainViewModel.missionDetailsList.forEach { mission ->
                            singleMission(mission, remove = { md ->
                                val newList = mainViewModel.missionDetailsList.toMutableList()
                                newList.remove(md)
                                mainViewModel.missionDetailsList = newList

                            }, moveToDetails = { misData ->
                                when (misData.missionName) {
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
                                        mainViewModel.whichMissionHandle(whichMissionHandler.thisMission(missionMemory = true, missionMath = false, missionShake = false, isSteps = false))
                                        controller.navigate(Routes.CommonMissionScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                                        mainViewModel.whichMissionHandle(whichMissionHandler.thisMission(missionMemory = false, missionMath = false, missionShake = true, isSteps = false))
                                        controller.navigate(Routes.CommonMissionScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                                        mainViewModel.whichMissionHandle(whichMissionHandler.thisMission(missionMemory = false, missionMath = true, missionShake = false, isSteps = false))
                                        controller.navigate(Routes.CommonMissionScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }

                                    }
                                    "Photo" -> {
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
                                            MissionDataHandler.ImageId(
                                                misData.imageId
                                            )
                                        )
                                        controller.navigate(Routes.CameraRoutineScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                        repeat(emptyStaticSlots) {
                            singleMission(
                                Missions(),
                                remove = {},
                                moveToDetails = {}) {
                                controller.navigate(Routes.MissionMenuScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(elementBack)
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
                        imageVector = if (soundVolume <= 0) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = "", tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = soundVolume,
                        onValueChange = { soundVolume = it },
                        enabled = true,
                        valueRange = 0f..100f,
                        onValueChangeFinished = {},
                        steps = 0,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xff12a8cb),
                            activeTrackColor = Color(0xff12a8cb),
                            inactiveTrackColor = Color(0xff3C3F48)
                        ), modifier = Modifier.fillMaxWidth(0.6f)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = "",
                        tint = if (switchState) Color.White else Color(0xffB6BDCA)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = switchState,
                        onCheckedChange = { newSwitchState ->
                            switchState = newSwitchState
                            // Handle the new switch state
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xff12A8CB), // Color when switch is ON
                            checkedTrackColor = Color(0xff18677E), // Track color when switch is ON
                            uncheckedThumbColor = Color(0xffB5BCCB), // Color when switch is OFF
                            uncheckedTrackColor = Color(0xff111217) // Track color when switch is OFF
                        ), modifier = Modifier.scale(0.8f)
                    )
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

                SingleOption(title = "Sound power-up", data = "off") {
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(elementBack)
            ) {
                SingleOption(
                    title = "Snooze",
                    data = if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) "${mainViewModel.selectedDataAlarm.snoozeTime} min" else if (!mainViewModel.whichAlarm.isOld && mainViewModel.newAlarm.snoozeTime != -1) "${mainViewModel.newAlarm.snoozeTime} min" else "off"
                ) {
                    controller.navigate(Routes.SnoozeScreen.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
                SingleOption(title = "Label", data = "none") {
                }
                SingleOption(title = "Sound", isLock = true, data = "Random play") {
                }
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
                            scheduleTheAlarm(
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
                            scheduleTheAlarm(
                                mainViewModel.newAlarm,
                                alarmScheduler, mainViewModel.whichAlarm.isOld,
                            ) { tomorrowTimeMillis, currentTimeMillis ->
                                remainingTime = tomorrowTimeMillis - currentTimeMillis
                            }
                            mainViewModel.newAlarmHandler(newAlarmHandler.insert)
                        }
                        showRemaining = true
                        controller.navigate(Routes.MainScreen.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }, text = "Save", width = 0.85f)
                }
            }
        }
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


fun getMathEqForSliderValue(value: String): String {
    return when (value) {
        "Very Easy" -> "3 + 4 = "
        "Easy" -> "16 + 25  = "
        "Normal" -> "43 + 32 + 34 = "
        "Hard" -> " (6 x 5) + 20 = "
        else -> "3 + 4 = "
    }
}

fun scheduleTheAlarm(
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
    isOld: Boolean,
    updateRemainingTime: (Long, Long) -> Unit
) {

    val selectedTimeMillis = localTimeToMillis(alarmEntity.localTime)
    Log.d("ALMD", "$selectedTimeMillis SELECTED MILLI SEC")
    // If the selected time has already passed today, calculate the days until the next occurrence
    if (alarmEntity.listOfDays.isNotEmpty()) {
        val calendar = Calendar.getInstance()

        // Find the next occurrence of the specified day
        val nextOccurrence = alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .filter { it >= calendar.get(Calendar.DAY_OF_WEEK) }
            .minOrNull() ?: alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .minOrNull()!!

        // Calculate the difference between the current day and the specified day
        val daysUntilNextOccurrence = nextOccurrence - calendar.get(Calendar.DAY_OF_WEEK)
        Log.d(
            "ALMD",
            "BEFORE SCHEDULING ${calendar.timeInMillis} and day to trigger is index is $nextOccurrence and days until are $daysUntilNextOccurrence"
        )
        Log.d("ALMD", "DAY OF WEEK ${calendar.get(Calendar.DAY_OF_WEEK)} ")
        Log.d(
            "ALMD",
            "DAYS UNTIL SUBTRACT RESULT ${nextOccurrence - calendar.get(Calendar.DAY_OF_WEEK)} "
        )
        if (daysUntilNextOccurrence < 0) {
            // Add 7 days to move to the next week
            val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrence)
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())

        } else if (selectedTimeMillis <= System.currentTimeMillis() && daysUntilNextOccurrence == 0) {
            if (alarmEntity.listOfDays.size == 1) {
                calendar.timeInMillis = selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
            } else {
                val nextOccurrenceAgain = alarmEntity.listOfDays
                    .map { getDayOfWeek(it) }
                    .filter { it > calendar.get(Calendar.DAY_OF_WEEK) }
                    .minOrNull() ?: alarmEntity.listOfDays
                    .map { getDayOfWeek(it) }
                    .minOrNull()!!
                val daysUntilNextOccurrenceAgain =
                    nextOccurrenceAgain - calendar.get(Calendar.DAY_OF_WEEK)
                if (daysUntilNextOccurrenceAgain < 0) {
                    // Add 7 days to move to the next week
                    val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrenceAgain)
                    calendar.timeInMillis =
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())

                } else {
                    calendar.timeInMillis =
                        selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrenceAgain.toLong())
                }
            }
        } else {
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
        }
        Log.d("ALMD", "AFTER SCHEDULING ${calendar.timeInMillis}")

        if (!isOld) {
            alarmEntity.id = calendar.timeInMillis
        }
        alarmEntity.timeInMillis = calendar.timeInMillis

        val instant = Instant.ofEpochMilli(calendar.timeInMillis)
        val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
        alarmEntity.localTime = offsetTime.toLocalTime()
        alarmEntity.reqCode = (0..19992).random()

        alarmScheduler.schedule(alarmEntity)

        // Update the remaining time for UI or other purposes
        updateRemainingTime(alarmEntity.timeInMillis, System.currentTimeMillis())

    } else {
        if (selectedTimeMillis <= System.currentTimeMillis() && alarmEntity.listOfDays.isEmpty()) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedTimeMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }

            if (!isOld) {
                alarmEntity.id = calendar.timeInMillis
            }
            alarmEntity.timeInMillis = calendar.timeInMillis

            val instant = Instant.ofEpochMilli(calendar.timeInMillis)
            val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
            alarmEntity.localTime = offsetTime.toLocalTime()
            alarmEntity.reqCode = (0..19992).random()

            alarmScheduler.schedule(alarmEntity)

            updateRemainingTime(alarmEntity.timeInMillis, System.currentTimeMillis())
        } else {
            // Schedule the alarm for the selected time
            if (!isOld) {
                alarmEntity.id = selectedTimeMillis
            }
            alarmEntity.reqCode = (0..19992).random()
            alarmEntity.timeInMillis = selectedTimeMillis
            alarmScheduler.schedule(alarmEntity)

            updateRemainingTime(selectedTimeMillis, System.currentTimeMillis())
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun singleMission(
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
        ) else BorderStroke(width = 2.dp, color = Color(0xffA6ACB5))
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
                        else -> {
                            Icons.Filled.AutoAwesomeMosaic
                        }
                    }
                    Icon(
                        imageVector = IconID,
                        contentDescription = "",
                        tint = Color.DarkGray
                    )
                    Text(
                        text = "${missionData.repeatTimes} times",
                        fontSize = 12.sp, color = Color.DarkGray
                    )

                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Add,
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
            color = Color.White,
            textAlign = TextAlign.Start, fontSize = 16.sp
        )
        if (isLock) {
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "",
                tint = Color.White,
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
                    data.trim(), fontSize = 15.sp,
                    letterSpacing = 0.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = "",
                    tint = Color.White.copy(
                        alpha = 0.6f
                    ),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    width: Float = 0.63f,
    title: String,
    actionText: String = "",
    backColor: Color = elementBack,
    onClick: () -> Unit,
) {
    TopAppBar(title = {
        Text(
            text = title,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth(width), fontSize = 16.sp
        )
    }, navigationIcon = {
        IconButton(onClick = { onClick() }) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIos,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
    }, actions = {
        Text(text = actionText, color = Color.White, fontSize = 13.sp)
    }, colors = topAppBarColors(
        containerColor = backColor,
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