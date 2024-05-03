package com.appdev.alarmapp.ui.MainScreen

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.AlarmCancelAccess
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.AlarmManagement.getDayOfWeek
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.findUpcomingAlarm
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.calculateTimeUntil
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import com.appdev.alarmapp.utils.getRepeatText
import com.appdev.alarmapp.utils.isOldOrNew
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    ringtoneRepository: RingtoneRepository,
    controller: NavHostController,
    mainViewModel: MainViewModel
) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var showSheetState by remember {
        mutableStateOf(false)
    }
    var stateChanges by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var clickAlarmId by remember {
        mutableLongStateOf(0)
    }
    var sharedPrefs by remember {
        mutableStateOf(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
    }
    val intent by remember {
        mutableStateOf(Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        })
    }

    val scope = rememberCoroutineScope()
    val alarmList by mainViewModel.alarmList.collectAsStateWithLifecycle(initialValue = emptyList())
    val alarmSettings by mainViewModel.basicSettings.collectAsStateWithLifecycle()
    var allAlarms by remember {
        mutableStateOf(emptyList<AlarmEntity>())
    }
    var showToast by remember {
        mutableStateOf(false)
    }
    var showToast2 by remember {
        mutableStateOf(false)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }

    val notificationService by remember { mutableStateOf(NotificationService(context)) }

    var upcomingAlarm by remember {
        mutableStateOf(findUpcomingAlarm(alarmList))
    }
    var alarmToPreview by remember {
        mutableStateOf(AlarmEntity())
    }
    var timeUntilNextAlarm by remember {
        mutableStateOf(upcomingAlarm?.let { calculateTimeUntil(it.nextTimeInMillis) })
    }

    val alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, ringtoneRepository))
    }
    LaunchedEffect(key1 = showToast, key2 = showToast2) {
        if (showToast) {
            Toast.makeText(context, "you can only skip enabled alarms", Toast.LENGTH_SHORT).show()
            showToast = false
        } else if (showToast2) {
            Toast.makeText(context, "you can only skip repeated alarms", Toast.LENGTH_SHORT).show()
            showToast2 = false
        }
    }
    LaunchedEffect(key1 = alarmList, key2 = stateChanges) {
        allAlarms = if (alarmSettings.activeSort) {
            val activeAlarms = alarmList.filter { it.isActive }
            val inactiveAlarms = alarmList.filter { !it.isActive }
            activeAlarms + inactiveAlarms
        } else {
            alarmList.sortedBy { it.nextTimeInMillis }
        }

        upcomingAlarm = alarmList
            .filter { it.isActive }
            .minByOrNull {
                if (it.nextTimeInMillis < System.currentTimeMillis()) {
                    checkForNextOccur(it)
                } else {
                    it.nextTimeInMillis
                }
            }
        timeUntilNextAlarm = upcomingAlarm?.let {
            if (it.nextTimeInMillis < System.currentTimeMillis()) {
                Log.d("CHJ", "${it.isOneTime}")
                calculateTimeUntil(checkForNextOccur(it))
            } else {
                calculateTimeUntil(it.nextTimeInMillis)
            }
        }
        timeUntilNextAlarm?.let {
            if (it.seconds < 0 || it.minutes < 0 || it.hours < 0 || it.days < 0) {
                upcomingAlarm = alarmList
                    .filter { it.isActive }
                    .minByOrNull {
                        if (it.nextTimeInMillis < System.currentTimeMillis()) {
                            checkForNextOccur(it)
                        } else {
                            it.nextTimeInMillis
                        }
                    }
                timeUntilNextAlarm = upcomingAlarm?.let {
                    if (it.nextTimeInMillis < System.currentTimeMillis()) {
                        Log.d("CHJ", "${it.isOneTime}")
                        calculateTimeUntil(checkForNextOccur(it))
                    } else {
                        calculateTimeUntil(it.nextTimeInMillis)
                    }
                }
            }
        }

        if (mainViewModel.basicSettings.value.showInNotification) {
            if (upcomingAlarm != null) {
                if (upcomingAlarm!!.isActive) {
                    notificationService.showNotification(
                        upcomingAlarm!!.localTime.toString() + " " + getAMPM(
                            upcomingAlarm!!.localTime
                        )
                    )
                }
            } else {
                notificationService.cancelNotification()
            }
        }
    }
    RepeatOnLifecycleEffect(action = {
        timeUntilNextAlarm = upcomingAlarm?.let {
            if (it.nextTimeInMillis < System.currentTimeMillis()) {
                Log.d("CHJ", "${it.isOneTime}")
                calculateTimeUntil(checkForNextOccur(it))
            } else {
                calculateTimeUntil(it.nextTimeInMillis)
            }
        }
    })


    val requestOverlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle the result if needed
        }

        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "This feature is crucial to this app", Toast.LENGTH_SHORT)
                .show()
        }
    }



    LaunchedEffect(key1 = Unit) {
        mainViewModel.missionData(MissionDataHandler.ResetList)
        if (mainViewModel.alarmID.value != 0L) {
            mainViewModel.updateAlarmID(0L)
        }
        if (!sharedPrefs.getBoolean(
                "battery",
                false
            )
        ) {
            showDialog = true
        }
        if (!Settings.canDrawOverlays(context)) {
            val intentNew = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            requestOverlayPermissionLauncher.launch(intentNew)
        }

        if (mainViewModel.missionDetails.repeatProgress > 1) {
            mainViewModel.missionData(MissionDataHandler.MissionProgress(1))
        }
        if (mainViewModel.previewMode) {
            mainViewModel.previewModeUpdate(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
                    .fillMaxHeight(0.12f),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape) // Clip the Box with CircleShape
                        .background(
                            if (isDarkMode) Color(0xff323232) else Color(0xFFDDF4FA),
                            CircleShape
                        )
                        .clickable {
                            controller.navigate(Routes.Purchase.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.diamond),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .padding(13.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f),
                onClick = { /*TODO*/ },
                border = if (isDarkMode) BorderStroke(
                    2.dp,
                    color = Color(0xff323232)
                ) else BorderStroke(2.dp, color = Color.LightGray.copy(alpha = 0.5f)),
                enabled = false,
                colors = CardDefaults.cardColors(disabledContainerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(15.dp), verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "Next Alarm",
                            fontSize = 17.sp,
                            letterSpacing = 0.sp,
                            color = if (isDarkMode) Color.LightGray else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (upcomingAlarm != null) {
                                when {
                                    (timeUntilNextAlarm?.days ?: 0) > 0 -> {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                "${timeUntilNextAlarm!!.days}",
                                                fontSize = 30.sp,
                                                letterSpacing = 0.sp,
                                                color = MaterialTheme.colorScheme.surfaceTint,
                                                fontWeight = FontWeight.W600
                                            )
                                            Text(
                                                if (timeUntilNextAlarm!!.days > 1) "days" else "day",
                                                fontSize = 16.sp,
                                                letterSpacing = 0.sp,
                                                color = MaterialTheme.colorScheme.surfaceTint,
                                                modifier = Modifier.padding(
                                                    bottom = 4.dp,
                                                    start = 4.dp
                                                )
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                "${timeUntilNextAlarm!!.hours}",
                                                fontSize = 30.sp,
                                                letterSpacing = 0.sp,
                                                color = MaterialTheme.colorScheme.surfaceTint,
                                                fontWeight = FontWeight.W600
                                            )
                                            Text(
                                                "hr",
                                                fontSize = 16.sp,
                                                letterSpacing = 0.sp,
                                                color = MaterialTheme.colorScheme.surfaceTint,
                                                modifier = Modifier.padding(
                                                    bottom = 4.dp,
                                                    start = 4.dp
                                                )
                                            )

                                        }
                                    }

                                    (timeUntilNextAlarm?.hours ?: 0) > 0 -> {
                                        // Display hours and minutes
                                        timeUntilNextAlarm?.let { tu ->
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    "${tu.hours}",
                                                    fontSize = 25.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "hr",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    modifier = Modifier.padding(
                                                        bottom = 4.dp,
                                                        start = 4.dp
                                                    )
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    "${tu.minutes}",
                                                    fontSize = 25.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "min",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    modifier = Modifier.padding(
                                                        bottom = 4.dp,
                                                        start = 4.dp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    else -> {
                                        timeUntilNextAlarm?.let { tu ->
                                            if (tu.minutes > 0) {
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        "${tu.minutes}",
                                                        fontSize = 25.sp,
                                                        letterSpacing = 0.sp,
                                                        color = MaterialTheme.colorScheme.surfaceTint,
                                                        fontWeight = FontWeight.W600
                                                    )
                                                    Text(
                                                        "min",
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.sp,
                                                        color = MaterialTheme.colorScheme.surfaceTint,
                                                        modifier = Modifier.padding(
                                                            bottom = 4.dp,
                                                            start = 4.dp
                                                        )
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    "${tu.seconds}",
                                                    fontSize = 25.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "sec",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = MaterialTheme.colorScheme.surfaceTint,
                                                    modifier = Modifier.padding(
                                                        bottom = 4.dp,
                                                        start = 4.dp
                                                    )
                                                )
                                            }
                                        }
                                        // If less than 24 hours, show hours and minutes

                                    }
                                }
                            } else {
                                Text(
                                    text = "No Upcoming alarms",
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                        }
                        CustomButton(
                            onClick = {
                                upcomingAlarm?.let { alarm ->
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.Vibrator(
                                            setVibration = alarm.willVibrate
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.CustomVolume(
                                            customVolume = alarm.customVolume
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.IsLabel(
                                            isLabelOrNot = alarm.isLabel
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.LabelText(
                                            getLabelText = alarm.labelTextForSpeech
                                        )
                                    )
                                    mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarm.isActive))
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.LoudEffect(
                                            isLoudEffectOrNot = alarm.isLoudEffect
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.TimeReminder(
                                            isTimeReminderOrNot = alarm.isTimeReminder
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.IsGentleWakeUp(
                                            isGentleWakeUp = alarm.isGentleWakeUp
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.GetWakeUpTime(
                                            getWUTime = alarm.wakeUpTime
                                        )
                                    )
                                    mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                                    mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getTime(
                                            time = alarm.localTime
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.AddList(
                                            missionsList = alarm.listOfMissions
                                        )
                                    )
                                    mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.isOneTime(
                                            isOneTime = alarm.isOneTime
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getSnoozeTime(
                                            getSnoozeTime = alarm.snoozeTime
                                        )
                                    )
                                    mainViewModel.isOld(isOldOrNew.isOld(true))
                                    controller.navigate(Routes.Preview.route) {
                                        popUpTo(controller.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            text = "Alarm Details",
                            backgroundColor = Color(0xff7B70FF),
                            width = 0.9f,
                            height = 40.dp,
                            fontSize = 15.sp, isEnabled = upcomingAlarm != null
                        )
                    }


                    Card(
                        onClick = { /*TODO*/ }, modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 10.dp), enabled = false,
                        colors = CardDefaults.cardColors(
                            disabledContainerColor = if (isDarkMode) Color(0xff313131) else Color(
                                0xFFBDEAF5
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 13.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isDarkMode) Color(0xff433E47) else Color(
                                            0xFFDDF4FA
                                        ),
                                        CircleShape
                                    )
                                    .clickable {
                                        controller.navigate(Routes.Purchase.route) {
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.addfriend),
                                    contentDescription = "",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Invite friends and",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)
                            )

                            Text(
                                text = "Unlock Premium",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            CustomButton(
                                onClick = { /*TODO*/ },
                                text = "Go",
                                backgroundColor = Color(0xffF18D44),
                                width = 0.45f,
                                height = 33.dp,
                                fontSize = 14.sp
                            )

                        }
                    }
                }

            }

            LazyColumn() {
                items(allAlarms, key = { alarm -> alarm.id }) { alarm ->
                    Log.d("CHKVM", "${alarm.listOfMissions}")
                    AlarmBox(isDarkMode, delete = {
                        alarmScheduler.cancel(alarm)
                        mainViewModel.deleteAlarm(it)
                        mainViewModel.deleteMissions(it)
                    }, onAlarmCLick = {
                        mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = alarm.willVibrate))
                        mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = alarm.customVolume))
                        mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = alarm.isLabel))
                        mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = alarm.labelTextForSpeech))
                        mainViewModel.updateHandler(EventHandlerAlarm.LoudEffect(isLoudEffectOrNot = alarm.isLoudEffect))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.TimeReminder(
                                isTimeReminderOrNot = alarm.isTimeReminder
                            )
                        )
                        mainViewModel.updateHandler(EventHandlerAlarm.IsGentleWakeUp(isGentleWakeUp = alarm.isGentleWakeUp))
                        mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = alarm.wakeUpTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarm.isActive))
                        mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                        mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                        mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = alarm.skipTheAlarm))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime)
                        )
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.getTime(
                                time = alarm.localTime
                            )
                        )
                        mainViewModel.missionData(MissionDataHandler.AddList(missionsList = alarm.listOfMissions))
                        mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                        mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarm.timeInMillis))
                        mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = alarm.nextTimeInMillis))
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = alarm.snoozeTime))

                        mainViewModel.isOld(isOldOrNew.isOld(true))
                        controller.navigate(Routes.Preview.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }, onAlarmBoxClick = { clickedAlarmId ->
                        clickAlarmId = clickedAlarmId
                        alarmToPreview = alarm
                        showSheetState = true
                    }, alarm) { on ->
                        stateChanges = true
                        mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                        mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                        mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                        mainViewModel.updateHandler(EventHandlerAlarm.LoudEffect(isLoudEffectOrNot = alarm.isLoudEffect))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.TimeReminder(
                                isTimeReminderOrNot = alarm.isTimeReminder
                            )
                        )
                        mainViewModel.updateHandler(EventHandlerAlarm.IsGentleWakeUp(isGentleWakeUp = alarm.isGentleWakeUp))
                        mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = alarm.wakeUpTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = alarm.willVibrate))
                        mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = alarm.customVolume))
                        mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = alarm.isLabel))
                        mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = alarm.labelTextForSpeech))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.getTime(
                                time = alarm.localTime
                            )
                        )
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime)
                        )
                        mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarm.timeInMillis))
                        mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = alarm.nextTimeInMillis))
                        mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = alarm.listOfMissions))
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = alarm.snoozeTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = on))
                        mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = false))

                        if (on) {
                            mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = alarm.skipTheAlarm))
                            com.appdev.alarmapp.ui.PreivewScreen.scheduleTheAlarm(
                                mainViewModel,
                                mainViewModel.basicSettings.value.showInNotification,
                                mainViewModel.selectedDataAlarm,
                                alarmScheduler,
                                true
                            ) { l, l1 ->
                            }
//                            scheduleTheAlarm(
//                                mainViewModel.selectedDataAlarm,
//                                alarmScheduler,
//                                mainViewModel.basicSettings.value.showInNotification,
//                                mainViewModel
//                            )
                        } else {

                            alarmScheduler.cancel(mainViewModel.selectedDataAlarm)
                        }
                        mainViewModel.updateHandler(EventHandlerAlarm.update)
                    }

                }
                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }

        }
    }
    if (showSheetState) {
        ModalBottomSheet(
            onDismissRequest = { showSheetState = false },
            sheetState = sheetState,
            dragHandle = {}) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.onBackground)) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            sheetState.hide()
                        }
                        showSheetState = false
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }
                singleSheetItem(name = "Delete", icon = Icons.Filled.Delete) {
                    alarmScheduler.cancel(alarmToPreview)
                    mainViewModel.deleteAlarm(clickAlarmId)
                    scope.launch {
                        sheetState.hide()
                    }
                    showSheetState = false
                }
                singleSheetItem(name = "Preview Alarm", icon = Icons.Filled.Alarm) {
                    val newIntent = Intent(context, AlarmCancelAccess::class.java)
                    newIntent.putExtra("Alarm", alarmToPreview)
                    newIntent.putExtra("Preview", true)
                    context.startActivity(newIntent)
                }
                singleSheetItem(
                    name = if (alarmToPreview.skipTheAlarm) "Unskip next alarm" else "Skip next alarm",
                    icon = Icons.Filled.SkipNext
                ) {
                    if (alarmToPreview.isActive) {
                        if (alarmToPreview.listOfDays.isNotEmpty()) {
                            if (alarmToPreview.skipTheAlarm) {
                                alarmScheduler.cancel(alarmToPreview)
                                alarmToPreview.skipTheAlarm = false

                                com.appdev.alarmapp.ui.PreivewScreen.scheduleTheAlarm(
                                    mainViewModel,
                                    mainViewModel.basicSettings.value.showInNotification,
                                    alarmToPreview,
                                    alarmScheduler,
                                    true
                                ) { l, l1 ->
                                    mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarmToPreview.id))
                                    mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarmToPreview.listOfDays))
                                    mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarmToPreview.ringtone))
                                    mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = false))
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.LoudEffect(
                                            isLoudEffectOrNot = alarmToPreview.isLoudEffect
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.TimeReminder(
                                            isTimeReminderOrNot = alarmToPreview.isTimeReminder
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.IsGentleWakeUp(
                                            isGentleWakeUp = alarmToPreview.isGentleWakeUp
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.GetWakeUpTime(
                                            getWUTime = alarmToPreview.wakeUpTime
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.Vibrator(
                                            setVibration = alarmToPreview.willVibrate
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.CustomVolume(
                                            customVolume = alarmToPreview.customVolume
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.IsLabel(
                                            isLabelOrNot = alarmToPreview.isLabel
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.LabelText(
                                            getLabelText = alarmToPreview.labelTextForSpeech
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getTime(
                                            time = alarmToPreview.localTime
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.isOneTime(isOneTime = alarmToPreview.isOneTime)
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getMissions(
                                            missions = alarmToPreview.listOfMissions
                                        )
                                    )
                                    mainViewModel.updateHandler(
                                        EventHandlerAlarm.getSnoozeTime(
                                            getSnoozeTime = alarmToPreview.snoozeTime
                                        )
                                    )
                                    mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarmToPreview.isActive))
                                    mainViewModel.updateHandler(EventHandlerAlarm.update)
                                }
//                                scheduleTheAlarm(
//                                    alarmToPreview,
//                                    alarmScheduler,
//                                    mainViewModel.basicSettings.value.showInNotification,
//                                    mainViewModel
//                                )
                            } else {
                                alarmScheduler.cancel(alarmToPreview)
                                val selectedTimeMillis = alarmToPreview.nextTimeInMillis
                                Log.d("CHKITO", "${selectedTimeMillis} tim milli")
                                val calendar = Calendar.getInstance()

                                val date = Date(selectedTimeMillis)

                                // Create a calendar instance and set the time to the given Date

                                calendar.time = date

                                // Get the day of the week from the calendar
                                var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                                Log.d("CHKITO", "${dayOfWeek} today indez")
                                // Convert day of week to day index starting from 1 for Sunday
//                                dayOfWeek = if (dayOfWeek == Calendar.SUNDAY) {
//                                    1
//                                } else {
//                                    dayOfWeek - Calendar.SUNDAY + 2
//                                }

                                val nextOccurrence = alarmToPreview.listOfDays
                                    .map { getDayOfWeek(it) }
                                    .filter { it > dayOfWeek }
                                    .minOrNull() ?: alarmToPreview.listOfDays
                                    .map { getDayOfWeek(it) }
                                    .minOrNull()!!
                                Log.d("CHKITO", "${nextOccurrence} upcoming indez")
                                val daysUntilNextOccurrence =
                                    nextOccurrence - dayOfWeek
                                if (daysUntilNextOccurrence < 0) {
                                    val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrence)
                                    calendar.timeInMillis =
                                        selectedTimeMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())
                                } else if (daysUntilNextOccurrence == 0) {
                                    calendar.timeInMillis =
                                        selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
                                } else {
                                    calendar.timeInMillis =
                                        selectedTimeMillis + TimeUnit.DAYS.toMillis(
                                            daysUntilNextOccurrence.toLong()
                                        )
                                }


                                val instant = Instant.ofEpochMilli(calendar.timeInMillis)
                                val offsetTime =
                                    OffsetTime.ofInstant(instant, ZoneId.systemDefault())
                                alarmToPreview.localTime = offsetTime.toLocalTime()

                                mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarmToPreview.id))
                                mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarmToPreview.listOfDays))
                                mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarmToPreview.ringtone))
                                mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = !alarmToPreview.skipTheAlarm))
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.LoudEffect(
                                        isLoudEffectOrNot = alarmToPreview.isLoudEffect
                                    )
                                )
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.TimeReminder(
                                        isTimeReminderOrNot = alarmToPreview.isTimeReminder
                                    )
                                )
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.IsGentleWakeUp(
                                        isGentleWakeUp = alarmToPreview.isGentleWakeUp
                                    )
                                )
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.GetWakeUpTime(
                                        getWUTime = alarmToPreview.wakeUpTime
                                    )
                                )
                                mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = alarmToPreview.willVibrate))
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.CustomVolume(
                                        customVolume = alarmToPreview.customVolume
                                    )
                                )
                                mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = alarmToPreview.isLabel))
                                mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = alarmToPreview.labelTextForSpeech))
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.getTime(
                                        time = alarmToPreview.localTime
                                    )
                                )
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.isOneTime(isOneTime = alarmToPreview.isOneTime)
                                )
                                mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarmToPreview.timeInMillis))
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.getNextMilli(
                                        upcomingMilli = calendar.timeInMillis
                                    )
                                )
                                mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = alarmToPreview.listOfMissions))
                                mainViewModel.updateHandler(
                                    EventHandlerAlarm.getSnoozeTime(
                                        getSnoozeTime = alarmToPreview.snoozeTime
                                    )
                                )
                                mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarmToPreview.isActive))
                                mainViewModel.updateHandler(EventHandlerAlarm.update)
                                alarmToPreview.timeInMillis = calendar.timeInMillis

                                alarmScheduler.schedule(
                                    alarmToPreview,
                                    mainViewModel.basicSettings.value.showInNotification
                                )
                            }
                        } else {
                            showToast2 = true
                        }
                    } else {
                        showToast = true
//                        if (alarmToPreview.listOfDays.isNotEmpty()) {
//                            alarmScheduler.cancel(alarmToPreview)
//                            mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarmToPreview.id))
//                            mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarmToPreview.listOfDays))
//                            mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarmToPreview.ringtone))
//                            mainViewModel.updateHandler(EventHandlerAlarm.skipAlarm(skipped = false))
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.LoudEffect(
//                                    isLoudEffectOrNot = alarmToPreview.isLoudEffect
//                                )
//                            )
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.TimeReminder(
//                                    isTimeReminderOrNot = alarmToPreview.isTimeReminder
//                                )
//                            )
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.IsGentleWakeUp(
//                                    isGentleWakeUp = alarmToPreview.isGentleWakeUp
//                                )
//                            )
//                            mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = alarmToPreview.wakeUpTime))
//                            mainViewModel.updateHandler(EventHandlerAlarm.Vibrator(setVibration = alarmToPreview.willVibrate))
//                            mainViewModel.updateHandler(EventHandlerAlarm.CustomVolume(customVolume = alarmToPreview.customVolume))
//                            mainViewModel.updateHandler(EventHandlerAlarm.IsLabel(isLabelOrNot = alarmToPreview.isLabel))
//                            mainViewModel.updateHandler(EventHandlerAlarm.LabelText(getLabelText = alarmToPreview.labelTextForSpeech))
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.getTime(
//                                    time = alarmToPreview.localTime
//                                )
//                            )
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.isOneTime(isOneTime = alarmToPreview.isOneTime)
//                            )
//                            mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarmToPreview.timeInMillis))
//                            mainViewModel.updateHandler(EventHandlerAlarm.getNextMilli(upcomingMilli = alarmToPreview.nextTimeInMillis))
//                            mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = alarmToPreview.listOfMissions))
//                            mainViewModel.updateHandler(
//                                EventHandlerAlarm.getSnoozeTime(
//                                    getSnoozeTime = alarmToPreview.snoozeTime
//                                )
//                            )
//                            mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarmToPreview.isActive))
//                            mainViewModel.updateHandler(EventHandlerAlarm.update)
//                        }
                    }
                    scope.launch {
                        sheetState.hide()
                    }
                    showSheetState = false
                }
                singleSheetItem(name = "Duplicate alarm", icon = Icons.Filled.ContentCopy) {

                    mainViewModel.insertAlarm(
                        AlarmEntity(
                            id = alarmToPreview.timeInMillis + (0..19992).random(),
                            timeInMillis = alarmToPreview.timeInMillis,
                            snoozeTime = alarmToPreview.snoozeTime,
                            snoozeTimeInMillis = 0,
                            localTime = alarmToPreview.localTime,
                            listOfDays = alarmToPreview.listOfDays,
                            isActive = alarmToPreview.isActive,
                            isOneTime = alarmToPreview.isOneTime,
                            ringtone = alarmToPreview.ringtone,
                            listOfMissions = alarmToPreview.listOfMissions,
                            labelTextForSpeech = alarmToPreview.labelTextForSpeech,
                            wakeUpTime = alarmToPreview.wakeUpTime,
                            isTimeReminder = alarmToPreview.isTimeReminder,
                            isLoudEffect = alarmToPreview.isLoudEffect,
                            isGentleWakeUp = alarmToPreview.isGentleWakeUp,
                            isLabel = alarmToPreview.isLabel,
                            customVolume = alarmToPreview.customVolume,
                            willVibrate = alarmToPreview.willVibrate,
                            skipTheAlarm = alarmToPreview.skipTheAlarm
                        )
                    )
                    scope.launch {
                        sheetState.hide()
                    }
                    showSheetState = false
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showDialog) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Dialog(onDismissRequest = {}) {
                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    Text(
                        text = "Alarm will not ring !",
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Alarms can't go off when Alarmy is forced off.Please exclude Alarm from battery optimization setting to avoid force termination",
                        fontSize = 15.sp,
                        letterSpacing = 0.sp,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 9.dp, start = 5.dp, end = 5.dp),
                        fontWeight = FontWeight.W400
                    )

                    Text(
                        "App Info -> Battery -> MANAGE BATTERY USAGE -> Unrestricted",
                        fontSize = 15.sp,
                        letterSpacing = 0.sp,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 9.dp, start = 5.dp, end = 5.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            CustomButton(
                                onClick = {
                                    showDialog = false
                                },
                                text = "Not now",
                                width = 0.40f,
                                backgroundColor = Color(0xff3F434F)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            CustomButton(
                                onClick = {
                                    showDialog = false
                                    context.startActivity(intent)
                                    sharedPrefs.edit().putBoolean("battery", true).apply()
                                },
                                text = "Go to Settings",
                                width = 0.8f,
                            )
                        }
                    }
                }
            }
        }

    }


}

fun checkForNextOccur(it: AlarmEntity): Long {
    val remainingTime: Long
    if (it.isOneTime) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = it.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }
        Log.d("CHKJ", "One time so ${calendar.timeInMillis}")
        remainingTime = calendar.timeInMillis
    } else {
        val calendar = Calendar.getInstance()
        val nextOccurrence = it.listOfDays
            .map { getDayOfWeek(it) }
            .filter { it > calendar.get(Calendar.DAY_OF_WEEK) }
            .minOrNull() ?: it.listOfDays
            .map { getDayOfWeek(it) }
            .minOrNull()!!

        val daysUntilNextOccurrence = nextOccurrence - calendar.get(Calendar.DAY_OF_WEEK)

        if (daysUntilNextOccurrence < 0) {
            val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrence)
            remainingTime =
                it.timeInMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())
        } else if (daysUntilNextOccurrence == 0) {
            remainingTime =
                it.timeInMillis + TimeUnit.DAYS.toMillis(7L)
        } else {
            remainingTime =
                it.timeInMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
        }
    }

    return remainingTime

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBox(
    isDarkMode: Boolean,
    delete: (Long) -> Unit,
    onAlarmCLick: () -> Unit,
    onAlarmBoxClick: (Long) -> Unit,
    alarm: AlarmEntity,
    updateAlarm: (Boolean) -> Unit
) {
    var switchState by remember { mutableStateOf(alarm.isActive) }

    Spacer(modifier = Modifier.height(10.dp))

//    val deleteIt = SwipeAction(
//        onSwipe = {
//            delete(alarm.id)
//        },
//        icon = {
//            Icon(
//                Icons.Default.Delete,
//                contentDescription = "Delete alarm",
//                modifier = Modifier.padding(16.dp),
//                tint = Color.White
//            )
//        }, background = Color.Red.copy(alpha = 0.5f),
//        isUndo = true
//    )
//    SwipeableActionsBox(
//        swipeThreshold = 50.dp,
//        startActions = listOf(deleteIt)
//    ) {
    Card(
        onClick = { onAlarmCLick() },
        modifier = Modifier
            .padding(horizontal = 23.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(start = 15.dp, end = 15.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTheTime(alarm.localTime.hour, alarm.localTime.minute),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    textDecoration = if (alarm.skipTheAlarm) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 30.sp, fontWeight = FontWeight.Medium
                )
                alarm.localTime.let {
                    Text(
                        text = getAMPM(it),
                        color = MaterialTheme.colorScheme.surfaceTint,
                        textDecoration = if (alarm.skipTheAlarm) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Switch(
                        checked = switchState,
                        onCheckedChange = { newSwitchState ->
                            switchState = newSwitchState
                            updateAlarm(switchState)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(start = 15.dp, end = 15.dp, bottom = 8.dp, top = 4.dp)
            ) {
                Text(
                    text = if (alarm.listOfDays.isNotEmpty()) {
                        getRepeatText(alarm.listOfDays)
                    } else if (alarm.isOneTime) {
                        "one-time"
                    } else {
                        "one-time"
                    },
                    color = Color.Gray,
                    fontSize = 14.sp, fontWeight = FontWeight.Medium
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    .padding(start = 15.dp, end = 25.dp, bottom = 8.dp, top = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mission ",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 15.sp
                )
                alarm.listOfMissions.forEach {
                    Spacer(modifier = Modifier.width(5.dp))
                    when (it.missionName) {
                        "Memory" -> {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesomeMosaic,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        "Shake" -> {
                            Icon(
                                imageVector = Icons.Filled.ScreenRotation,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        "Math" -> {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        "Photo" -> {
                            Icon(
                                imageVector = Icons.Filled.CameraEnhance,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        "QR/Barcode" -> {
                            Icon(
                                imageVector = Icons.Filled.QrCode2,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        "Typing" -> {
                            Icon(
                                imageVector = Icons.Filled.Keyboard,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "ArrangeNumbers" -> {
                            Icon(
                                imageVector = Icons.Filled.RepeatOne,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "ArrangeAlphabet" -> {
                            Icon(
                                imageVector = Icons.Filled.CompareArrows,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "ArrangeShapes" -> {
                            Icon(
                                imageVector = Icons.Filled.Pentagon,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "RangeNumbers" -> {
                            Icon(
                                imageVector = Icons.Filled.LooksOne,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "RangeAlphabet" -> {
                            Icon(
                                imageVector = Icons.Filled.SortByAlpha,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "ReachDestination" -> {
                            Icon(
                                imageVector = Icons.Filled.AddLocationAlt,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        "WalkOff" -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (alarm.listOfMissions.isEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(), contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                //ASSIGN CLICKED MEMEBR AT TOP THE PASSED VALUE IN THIS COMPOSABLE
                                onAlarmBoxClick(alarm.id)
                            }, contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = if (isDarkMode) R.drawable.dots else R.drawable.dotsblack),
                            contentDescription = "",
                            modifier = Modifier
                                .size(25.dp)
                                .rotate(90f)
                        )
                    }
                }
            }
        }

    }
//    }
}

@Composable
fun RepeatOnLifecycleEffect(
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    action: suspend CoroutineScope.() -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current

    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(state, block = action)
    }
}


fun formattedTheTime(hour: Int, minute: Int): String {
    val formatHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
    return String.format("%02d:%02d", formatHour, minute)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getAMPM(localTime: LocalTime): String {
    val amPm = if (localTime.hour < 12) "am" else "pm"
    return amPm
}

@Composable
fun singleSheetItem(name: String, icon: ImageVector, onCLick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCLick() }
            .padding(vertical = 10.dp, horizontal = 20.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.75f)
        )
        Text(
            text = name,
            modifier = Modifier.padding(start = 10.dp),
            color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.75f)
        )
    }
}

private fun isNextAlarmOnDifferentDay(alarmTimeInMillis: Long): Boolean {
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = alarmTimeInMillis
    val alarmDay = calendar.get(Calendar.DAY_OF_YEAR)

    return currentDay != alarmDay
}

private fun isAlarmDayMatch(alarm: AlarmEntity): Boolean {
    val calendar = Calendar.getInstance()
    val currentDayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)

    return alarm.listOfDays.contains(currentDayName)
}