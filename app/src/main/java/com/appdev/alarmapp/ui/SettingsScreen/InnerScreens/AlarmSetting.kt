package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettings(mainViewModel: MainViewModel, controller: NavHostController) {
    val alarmList by mainViewModel.alarmList.collectAsStateWithLifecycle(initialValue = emptyList())
    val alarmSettings = mainViewModel.basicSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationService by remember { mutableStateOf(NotificationService(context)) }


    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(backColor)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.67f)
                    .padding(vertical = 10.dp, horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        controller.popBackStack()
                    },
                    border = BorderStroke(1.dp, Color.White),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(23.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Alarm Setting",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            settingsElement(title = "Default Setting for New Alarms", onClick = {
                mainViewModel.setDefaultSettings(DefaultSettingsHandler.GoingToSetDefault(true))
                controller.navigate(Routes.DefaultSettingsScreen.route) {
                    popUpTo(Routes.SettingsOfAlarmScreen.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            })
            Text(
                "Set by default in editor when setting alarms",
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                color = Color(0xffA6ACB5),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp, vertical = 7.dp)
            )
            Text(
                "Upcoming alarms",
                fontSize = 14.sp,
                letterSpacing = 0.sp,
                color = Color(0xffA6ACB5),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 30.dp)
            )
            settingsElement(
                title = "Show next alarm in notification drawer",
                onClick = {

                },
                isSwitch = true, switchState = alarmSettings.value.showInNotification
            ) {
                val upComingAlarm = findUpcomingAlarm(alarmList)
                mainViewModel.updateBasicSettings(
                    AlarmSetting(
                        id = mainViewModel.basicSettings.value.id,
                        showInNotification = it,
                        activeSort = mainViewModel.basicSettings.value.activeSort
                    )
                )
                if (it) {
                    val upcomingAlarmTime = upComingAlarm?.localTime?.toString()
                    upcomingAlarmTime?.let {
                        notificationService.showNotification(it + " " + getAMPM(upComingAlarm.localTime))
                    }
                } else {
                    // Cancel the notification
                    notificationService.cancelNotification()
                }
            }
            settingsElement(
                title = "Sort by enabled alarm first",
                onClick = { /*TODO*/ },
                isSwitch = true,
                switchState = alarmSettings.value.activeSort
            ) {
                mainViewModel.updateBasicSettings(
                    AlarmSetting(
                        id = mainViewModel.basicSettings.value.id,
                        showInNotification = mainViewModel.basicSettings.value.showInNotification,
                        activeSort = it
                    )
                )
            }
//            Text(
//                "Alarm cheat prevention",
//                fontSize = 14.sp,
//                letterSpacing = 0.sp,
//                color = Color(0xffA6ACB5),
//                textAlign = TextAlign.Start,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 20.dp, top = 30.dp)
//            )
//            settingsElement(
//                title = "Prevent app uninstall during alarm",
//                onClick = { /*TODO*/ },
//                isSwitch = true
//            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsElement(
    resourceID: Int = -1,
    title: String,
    onClick: () -> Unit,
    isSwitch: Boolean = false,
    switchState: Boolean = false,
    onChangeState: (Boolean) -> Unit = {}
) {
    var switchState by remember { mutableStateOf(switchState) }
    Column {
        Spacer(modifier = Modifier.height(9.dp))
        Card(
            onClick = {
                if (!isSwitch) {
                    onClick()
                }
            },
            modifier = Modifier
                .height(68.dp)
                .padding(horizontal = 15.dp),
            shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed ,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xff24272E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resourceID != -1) {
                    Image(
                        painter = painterResource(id = resourceID),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                )
                if (isSwitch) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Switch(
                            checked = switchState,
                            onCheckedChange = { newSwitchState ->
                                switchState = newSwitchState
                                onChangeState(newSwitchState)
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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 3.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
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
    }
}

fun findUpcomingAlarm(alarms: List<AlarmEntity>): AlarmEntity? {
    // Get the current day and time
    val currentDay = LocalDate.now().dayOfWeek.toString()
    val currentTime = LocalTime.now()

    val filteredAlarms = alarms.filter { alarm ->
        alarm.listOfDays.contains(currentDay) && alarm.localTime >= currentTime &&
                alarm.isActive
    }
    return filteredAlarms.minByOrNull { it.timeInMillis }
}
