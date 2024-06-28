package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.DeviceAdminManage.DeviceAdminReceiver
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import java.time.LocalDate
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettings(
    mainViewModel: MainViewModel,
    controller: NavHostController,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val alarmList by mainViewModel.alarmList.collectAsStateWithLifecycle(initialValue = emptyList())
    val alarmSettings = mainViewModel.basicSettings.collectAsStateWithLifecycle()
    val defaultSettings = mainViewModel.defaultSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationService by remember { mutableStateOf(NotificationService(context)) }
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var showDialog by remember {
        mutableStateOf(false)
    }
    var showDialog_two by remember {
        mutableStateOf(false)
    }
    var showDialog_instruction by remember {
        mutableStateOf(false)
    }
    val deviceAdminComponent by remember {
        mutableStateOf(ComponentName(context, DeviceAdminReceiver::class.java))
    }
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }

    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
    }
    val devicePolicyManager by remember {
        mutableStateOf(context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
    }
    val serviceIntent by remember {
        mutableStateOf(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        )
    }
    var switchStatePrevention by remember {
        mutableStateOf(
            alarmSettings.value.preventUninstall && devicePolicyManager.isAdminActive(
                deviceAdminComponent
            )
        )
    }
    var switchScreenOffPrevention by remember {
        mutableStateOf(
            alarmSettings.value.preventPhoneOff && isAccessServiceEnabled(
                context
            )
        )
    }

    val accessibilityServicePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (isAccessServiceEnabled(context)) {
            switchScreenOffPrevention = true
            mainViewModel.updateBasicSettings(
                AlarmSetting(
                    id = mainViewModel.basicSettings.value.id,
                    showInNotification = mainViewModel.basicSettings.value.showInNotification,
                    activeSort = mainViewModel.basicSettings.value.activeSort,
                    preventUninstall = mainViewModel.basicSettings.value.preventUninstall,
                    preventPhoneOff = true
                )
            )
        } else {
            switchScreenOffPrevention = false
        }
    }


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
//                    mainViewModel.missionData(MissionDataHandler.ResetList)

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
                        activeSort = mainViewModel.basicSettings.value.activeSort,
                        preventUninstall = mainViewModel.basicSettings.value
                            .preventUninstall,
                        preventPhoneOff = mainViewModel.basicSettings.value.preventPhoneOff
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
                        activeSort = it,
                        preventUninstall = mainViewModel.basicSettings.value
                            .preventUninstall,
                        preventPhoneOff = mainViewModel.basicSettings.value.preventPhoneOff
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
                    .padding(start = 20.dp, top = 30.dp, bottom = 10.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R || Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
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
                                text = "Prevent phone turn-off during alarm",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Switch(
                                    checked = switchScreenOffPrevention,
                                    onCheckedChange = { newSwitchState ->
                                        if (newSwitchState) {
                                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && !isAccessServiceEnabled(
                                                    context
                                                )){
                                                    showDialog_instruction = true
                                                } else {
                                                showDialog_two = true
                                            }
                                        } else {
                                            switchScreenOffPrevention = newSwitchState
                                            mainViewModel.updateBasicSettings(
                                                AlarmSetting(
                                                    id = mainViewModel.basicSettings.value.id,
                                                    showInNotification = mainViewModel.basicSettings.value.showInNotification,
                                                    activeSort = mainViewModel.basicSettings.value.activeSort,
                                                    preventUninstall = mainViewModel.basicSettings.value.preventUninstall,
                                                    preventPhoneOff = false
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
                                                preventUninstall = false,
                                                preventPhoneOff = mainViewModel.basicSettings.value.preventPhoneOff
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
        if (showDialog_instruction) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 22.dp)
                            .background(
                                MaterialTheme.colorScheme.onBackground,
                                shape = RoundedCornerShape(5.dp)
                            ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Basic Instructions",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp), fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "1. Long press on Alarmy app then click on App info or details \n\n2. Tap on 3 dots ( \u22EE ) at top right corner \n\n3. Allow Restricted Settings",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                        Row( modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text(
                                text = "Note: ",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W600
                            )
                            Text(
                                text = "( ⋮ ) 3-dots will not appear if restricted settings are allowed for application",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W400
                            )
                        }
                        CustomButton(
                            onClick = {
                                showDialog_instruction = false
                                showDialog_two = true
                            },
                            text = "Next",
                            width = 0.90f,
                            backgroundColor = Color(0xffC5CDDA), textColor = Color.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
        if (showDialog_two) {
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
                            text = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && !isAccessServiceEnabled(
                                    context
                                )
                            ) "Permission for Accessibility Service" else "Caution",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp), fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && !isAccessServiceEnabled(
                                    context
                                )
                            ) "Find the Alarmy in downloaded apps list and grant the permission.\nBy enabling the Accessibility Service, you agree to allowing Alarmy to " +
                                    "detect the system Ul of turning the device off.\n" +
                                    "The sole purpose is to prevent you from turning the device off while your alarm is ringing and won't be " +
                                    "used or shared for any other purpose" else "If you set this on, app doesn't turned off during ringing.",
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp)
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
                                        switchScreenOffPrevention = false
                                        showDialog_two = false
                                    },
                                    text = "Cancel",
                                    width = 0.40f,
                                    backgroundColor = Color(0xffC5CDDA), textColor = Color.Black
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                CustomButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && !isAccessServiceEnabled(
                                                context
                                            )
                                        ) {
                                            accessibilityServicePermissionLauncher.launch(
                                                serviceIntent
                                            )
                                        } else {
                                            mainViewModel.updateBasicSettings(
                                                AlarmSetting(
                                                    id = mainViewModel.basicSettings.value.id,
                                                    showInNotification = mainViewModel.basicSettings.value.showInNotification,
                                                    activeSort = mainViewModel.basicSettings.value.activeSort,
                                                    preventUninstall = mainViewModel.basicSettings.value.preventUninstall,
                                                    preventPhoneOff = true
                                                )
                                            )
                                            switchScreenOffPrevention = true
                                        }
                                        showDialog_two = false
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

fun isAccessServiceEnabled(context: Context): Boolean {
    val enabledOrNot = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED
    );
    if (enabledOrNot == 1) {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return !(enabledServices == null || !enabledServices.contains(context.packageName))
    }
    return false
}