package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appdev.alarmapp.DeviceAdminManage.DeviceAdminReceiver
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.MissionDataHandler
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettings(mainViewModel: MainViewModel, controller: NavHostController) {
    val alarmList by mainViewModel.alarmList.collectAsStateWithLifecycle(initialValue = emptyList())
    val alarmSettings = mainViewModel.basicSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationService by remember { mutableStateOf(NotificationService(context)) }
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var showDialog by remember {
        mutableStateOf(false)
    }
    val deviceAdminComponent by remember {
        mutableStateOf(ComponentName(context, DeviceAdminReceiver::class.java))
    }

    val devicePolicyManager by remember {
        mutableStateOf(context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
    }

    var switchStatePrevention by remember { mutableStateOf(alarmSettings.value.preventUninstall && devicePolicyManager.isAdminActive(deviceAdminComponent)) }

    val requestOverlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        showDialog = false
        if (result.resultCode == Activity.RESULT_OK) {
            switchStatePrevention = true
            mainViewModel.updateBasicSettings(
                AlarmSetting(
                    id = mainViewModel.basicSettings.value.id,
                    showInNotification = mainViewModel.basicSettings.value.showInNotification,
                    activeSort = mainViewModel.basicSettings.value.activeSort,
                    preventUninstall = true
                )
            )
        }

        if (!devicePolicyManager.isAdminActive(deviceAdminComponent)) {
            Toast.makeText(
                context,
                "This Activation is Required to prevent un-installation of app",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(23.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }

                Text(
                    text = "Alarm Setting",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            settingsElement(
                devicePolicyManager,
                deviceAdminComponent,
                isDarkMode,
                title = "Default Setting for New Alarms",
                onClick = {
                    mainViewModel.missionData(MissionDataHandler.ResetList)
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
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp, vertical = 7.dp)
            )
            Text(
                "Upcoming alarms",
                fontSize = 14.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 30.dp)
            )
            settingsElement(
                devicePolicyManager, deviceAdminComponent,
                isDarkMode = isDarkMode,
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
                devicePolicyManager, deviceAdminComponent,
                isDarkMode = isDarkMode,
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
            Text(
                "Alarm cheat prevention",
                fontSize = 14.sp,
                letterSpacing = 0.sp,
                color = Color(0xffA6ACB5),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 30.dp)
            )
            Column {
                Spacer(modifier = Modifier.height(9.dp))
                Card(
                    onClick = {},
                    modifier = Modifier
                        .height(68.dp)
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed ,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prevent app uninstall during alarm",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Switch(
                                checked = switchStatePrevention,
                                onCheckedChange = { newSwitchState ->
                                    Log.d("CHKSB", "$newSwitchState")
                                    if (!devicePolicyManager.isAdminActive(deviceAdminComponent)) {
                                        showDialog = true
                                    } else {
                                        switchStatePrevention = newSwitchState
                                        devicePolicyManager.removeActiveAdmin(deviceAdminComponent)
                                        mainViewModel.updateBasicSettings(
                                            AlarmSetting(
                                                id = mainViewModel.basicSettings.value.id,
                                                showInNotification = mainViewModel.basicSettings.value.showInNotification,
                                                activeSort = mainViewModel.basicSettings.value.activeSort,
                                                preventUninstall = false
                                            )
                                        )
                                    }
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
                }
            }
        }
        if (showDialog) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                }) {
                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onBackground,
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Text(
                            text = "Prevent App Uninstall",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp), fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "during alarm",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(), fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "If you set this on, you can't uninstall Alarmy. If you want to uninstall Alarmy please tum off this option.",
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp)
                        )

                        Text(
                            "(This needs Device Manager permission)", fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp, start = 18.dp, end = 18.dp)
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
                                    text = "Cancel",
                                    width = 0.40f,
                                    backgroundColor = Color(0xffC5CDDA), textColor = Color.Black
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                CustomButton(
                                    onClick = {
                                        if (devicePolicyManager.isAdminActive(deviceAdminComponent)) {
                                            //do whatever is needed here is its active
                                            showDialog = false
                                        } else {
                                            val intent =
                                                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                                            intent.putExtra(
                                                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                                deviceAdminComponent
                                            )
                                            requestOverlayPermissionLauncher.launch(intent)
                                        }
                                    },
                                    text = "Ok",
                                    width = 0.75f,
                                    backgroundColor = Color(0xffC5CDDA), textColor = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsElement(
    devicePolicyManager: DevicePolicyManager,
    componentName: ComponentName,
    isDarkMode: Boolean,
    resourceID: Int = -1,
    title: String,
    onClick: () -> Unit,
    isSwitch: Boolean = false,
    switchState: Boolean = false,
    checkAdmin: Boolean = false,
    onActivateAdmin: () -> Unit = {},
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
                containerColor = MaterialTheme.colorScheme.inverseOnSurface
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
                    color = MaterialTheme.colorScheme.surfaceTint,
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
                                Log.d("CHKSB", "$newSwitchState")
                                if (checkAdmin && !devicePolicyManager.isAdminActive(componentName)) {
                                    onActivateAdmin() // Callback to trigger admin activation
                                } else {
                                    switchState = newSwitchState
                                    onChangeState(newSwitchState)
                                }
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
                            tint = MaterialTheme.colorScheme.surfaceTint.copy(
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
