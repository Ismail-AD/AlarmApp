package com.appdev.alarmapp.ui.MainScreen

import android.app.Activity
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.CustomImageButton
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.PreivewScreen.localTimeToMillis
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.findUpcomingAlarm
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.linear
import com.appdev.alarmapp.ui.theme.redOne
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Updating
import com.appdev.alarmapp.utils.calculateTimeUntil
import com.appdev.alarmapp.utils.getRepeatText
import com.appdev.alarmapp.utils.isOldOrNew
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(controller: NavHostController, mainViewModel: MainViewModel) {
    var showSheetState by remember {
        mutableStateOf(false)
    }
    var stateChanges by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    var clickAlarmId by remember {
        mutableLongStateOf(0)
    }
    val scope = rememberCoroutineScope()
    val alarmList by mainViewModel.alarmList.collectAsStateWithLifecycle(initialValue = emptyList())
    val alarmSettings by mainViewModel.basicSettings.collectAsStateWithLifecycle()
    var allAlarms by remember {
        mutableStateOf(emptyList<AlarmEntity>())
    }
    val context = LocalContext.current
    val alarmScheduler by remember {
        mutableStateOf(
            AlarmScheduler(
                context,
                mainViewModel
            )
        )
    }
    val notificationService by remember { mutableStateOf(NotificationService(context)) }

    var upcomingAlarm by remember {
        mutableStateOf(alarmList
            .filter { it.isActive && it.timeInMillis > System.currentTimeMillis() }
            .minByOrNull { it.timeInMillis })
    }
    var timeUntilNextAlarm by remember {
        mutableStateOf(upcomingAlarm?.let { calculateTimeUntil(it.timeInMillis) })
    }
    LaunchedEffect(key1 = alarmList, key2 = stateChanges) {
        allAlarms = if (alarmSettings.activeSort) {
            val activeAlarms = alarmList.filter { it.isActive }
            val inactiveAlarms = alarmList.filter { !it.isActive }
            activeAlarms + inactiveAlarms
        } else {
            alarmList.sortedBy { it.timeInMillis }
        }
        upcomingAlarm = alarmList
            .filter { it.isActive && it.timeInMillis > System.currentTimeMillis() }
            .minByOrNull { it.timeInMillis }
        timeUntilNextAlarm = upcomingAlarm?.let { calculateTimeUntil(it.timeInMillis) }
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
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            requestOverlayPermissionLauncher.launch(intent)
        }
        if (mainViewModel.missionDetails.repeatProgress > 1) {
            mainViewModel.missionData(MissionDataHandler.MissionProgress(1))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backColor)
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
                        .background(Color(0xff222325), CircleShape)
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
                border = BorderStroke(2.dp, color = Color(0xff272729)),
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
                            "Next Alarm", fontSize = 17.sp,
                            letterSpacing = 0.sp,
                            color = Color.LightGray, textAlign = TextAlign.Center
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
                                                "${timeUntilNextAlarm!!.days}", fontSize = 30.sp,
                                                letterSpacing = 0.sp,
                                                color = Color.White, fontWeight = FontWeight.W600
                                            )
                                            Text(
                                                "days",
                                                fontSize = 16.sp,
                                                letterSpacing = 0.sp,
                                                color = Color.White,
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
                                                    color = Color.White,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "hr",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = Color.White,
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
                                                    color = Color.White,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "min",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = Color.White,
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
                                                        color = Color.White,
                                                        fontWeight = FontWeight.W600
                                                    )
                                                    Text(
                                                        "min",
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.sp,
                                                        color = Color.White,
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
                                                    color = Color.White,
                                                    fontWeight = FontWeight.W600
                                                )
                                                Text(
                                                    "sec",
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    color = Color.White,
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
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                        }
                        CustomButton(
                            onClick = { /*TODO*/ },
                            text = "Alarm Details",
                            backgroundColor = Color(0xff7B70FF),
                            width = 0.9f,
                            height = 40.dp,
                            fontSize = 15.sp
                        )
                    }


                    Card(
                        onClick = { /*TODO*/ }, modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 10.dp), enabled = false,
                        colors = CardDefaults.cardColors(disabledContainerColor = Color(0xff222325))
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
                                    .background(Color(0xff333436), CircleShape)
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
                                color = Color.White,
                                fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)
                            )

                            Text(
                                text = "Unlock Premium",
                                color = Color.White,
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
                    AlarmBox(delete = {
                        mainViewModel.deleteAlarm(it)
                    }, onAlarmCLick = {
                        mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = alarm.isActive))
                        mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                        mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.getTime(
                                time = alarm.localTime
                            )
                        )
                        mainViewModel.missionData(MissionDataHandler.AddList(missionsList = alarm.listOfMissions))
                        mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                        mainViewModel.updateHandler(EventHandlerAlarm.requestCode(reqCode = alarm.reqCode))
                        mainViewModel.updateHandler(EventHandlerAlarm.isOneTime(isOneTime = alarm.isOneTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = alarm.snoozeTime))
                        mainViewModel.isOld(isOldOrNew.isOld(true))
                        controller.navigate(Routes.Preview.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }, onAlarmBoxClick = { clickedAlarmId ->
                        clickAlarmId = clickedAlarmId
                        showSheetState = true
                    }, alarm) { on ->
//                        stateChanges = true
                        mainViewModel.updateHandler(EventHandlerAlarm.idAlarm(iD = alarm.id))
                        mainViewModel.updateHandler(EventHandlerAlarm.getDays(days = alarm.listOfDays))
                        mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = alarm.ringtone))
                        mainViewModel.updateHandler(
                            EventHandlerAlarm.getTime(
                                time = alarm.localTime
                            )
                        )
                        mainViewModel.updateHandler(EventHandlerAlarm.getMilli(timeInMilli = alarm.timeInMillis))
                        mainViewModel.updateHandler(EventHandlerAlarm.requestCode(reqCode = alarm.reqCode))
                        mainViewModel.updateHandler(EventHandlerAlarm.getMissions(missions = alarm.listOfMissions))
                        mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = alarm.snoozeTime))
                        mainViewModel.updateHandler(EventHandlerAlarm.isActive(isactive = on))
                        mainViewModel.updateHandler(EventHandlerAlarm.update)
                        if (on) {
                            alarmScheduler.schedule(
                                alarm,
                                mainViewModel.basicSettings.value.showInNotification
                            )
                        } else {
                            alarmScheduler.cancel(alarm)
                        }
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
            Column(modifier = Modifier.background(Color(0xff1C1F26))) {
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
                            tint = Color.White
                        )
                    }
                }
                singleSheetItem(name = "Delete", icon = Icons.Filled.Delete) {
                    mainViewModel.deleteAlarm(clickAlarmId)
                    scope.launch {
                        sheetState.hide()
                    }
                    showSheetState = false
                }
                singleSheetItem(name = "Preview Alarm", icon = Icons.Filled.Alarm) {

                }
                singleSheetItem(name = "Skip next alarm", icon = Icons.Filled.SkipNext) {

                }
                singleSheetItem(name = "Duplicate alarm", icon = Icons.Filled.ContentCopy) {

                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBox(
    delete: (Long) -> Unit,
    onAlarmCLick: () -> Unit,
    onAlarmBoxClick: (Long) -> Unit,
    alarm: AlarmEntity,
    updateAlarm: (Boolean) -> Unit
) {
    var switchState by remember { mutableStateOf(alarm.isActive) }

    Spacer(modifier = Modifier.height(10.dp))

    val deleteIt = SwipeAction(
        onSwipe = {
            delete(alarm.id)
        },
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete chat",
                modifier = Modifier.padding(16.dp),
                tint = Color.White
            )
        }, background = Color.Red.copy(alpha = 0.5f),
        isUndo = true
    )
    SwipeableActionsBox(
        swipeThreshold = 50.dp,
        startActions = listOf(deleteIt)
    ) {
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
                        .background(Color(0xff222325))
                        .padding(start = 15.dp, end = 15.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTheTime(alarm.localTime.hour, alarm.localTime.minute),
                        color = Color.White,
                        fontSize = 30.sp, fontWeight = FontWeight.Medium
                    )
                    alarm.localTime.let {
                        Text(
                            text = getAMPM(it),
                            color = Color.White,
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
                                checkedThumbColor = Color.White, // Color when switch is ON
                                checkedTrackColor = Color(0xff7358F5), // Track color when switch is ON
                                uncheckedThumbColor = Color(0xff949495), // Color when switch is OFF
                                uncheckedTrackColor = Color(0xff343435) // Track color when switch is OFF
                            ), modifier = Modifier.scale(0.8f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xff222325))
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
                        .background(Color(0xff2A2A2C))
                        .padding(start = 15.dp, end = 25.dp, bottom = 8.dp, top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mission ",
                        color = Color(0xFF8572EB),
                        fontSize = 15.sp
                    )
                    alarm.listOfMissions.forEach {
                        Spacer(modifier = Modifier.width(5.dp))
                        when (it.missionName) {
                            "Memory" -> {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesomeMosaic,
                                    contentDescription = "",
                                    tint = Color(0xFF8572EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            "Shake" -> {
                                Icon(
                                    imageVector = Icons.Filled.ScreenRotation,
                                    contentDescription = "",
                                    tint = Color(0xFF8572EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            "Math" -> {
                                Icon(
                                    imageVector = Icons.Filled.Calculate,
                                    contentDescription = "",
                                    tint = Color(0xFF8572EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    if (alarm.listOfMissions.isEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "",
                            tint = Color(0xFF8572EB),
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
                                painter = painterResource(id = R.drawable.dots),
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
        Icon(imageVector = icon, contentDescription = "", tint = Color.White.copy(alpha = 0.75f))
        Text(
            text = name,
            modifier = Modifier.padding(start = 10.dp),
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

