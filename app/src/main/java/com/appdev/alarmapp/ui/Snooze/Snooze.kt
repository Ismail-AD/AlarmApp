package com.appdev.alarmapp.ui.Snooze

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.TopBar
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.listOfIntervals
import com.appdev.alarmapp.utils.newAlarmHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeScreen(controller: NavHostController, mainViewModel: MainViewModel) {
    var switchState by remember { mutableStateOf(if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime != -1) true else if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime == -1) false else if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) true else !mainViewModel.whichAlarm.isOld && mainViewModel.newAlarm.snoozeTime != -1) }
    var selectedOption by remember { mutableStateOf(if (mainViewModel.managingDefault && mainViewModel.defaultSettings.value.snoozeTime != -1) mainViewModel.defaultSettings.value.snoozeTime.toString() else if (mainViewModel.whichAlarm.isOld && mainViewModel.selectedDataAlarm.snoozeTime != -1) mainViewModel.selectedDataAlarm.snoozeTime.toString() else mainViewModel.newAlarm.snoozeTime.toString()) }
    Scaffold(topBar = {
        TopBar(width = 0.85f, title = "Snooze", actionText = "", backColor = backColor) {
            if (mainViewModel.managingDefault) {
                if (!switchState) {
                    mainViewModel.setDefaultSettings(
                        DefaultSettingsHandler.GetNewObject(defaultSettings = DefaultSettings(
                            id = mainViewModel.defaultSettings.value.id,
                            ringtone = mainViewModel.defaultSettings.value.ringtone,
                            snoozeTime = -1,
                            listOfMissions = mainViewModel.defaultSettings.value.listOfMissions
                        )
                        )
                    )
                } else {
                    mainViewModel.setDefaultSettings(
                        DefaultSettingsHandler.GetNewObject(defaultSettings = DefaultSettings(
                            id = mainViewModel.defaultSettings.value.id,
                            ringtone = mainViewModel.defaultSettings.value.ringtone,
                            snoozeTime = selectedOption.toInt(),
                            listOfMissions = mainViewModel.defaultSettings.value.listOfMissions
                        ))
                    )
                }
                mainViewModel.setDefaultSettings(DefaultSettingsHandler.UpdateDefault)
                controller.popBackStack()
            } else {
                if (mainViewModel.whichAlarm.isOld && switchState) {
                    mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = selectedOption.toInt()))
                } else if (!mainViewModel.whichAlarm.isOld && switchState) {
                    mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = selectedOption.toInt()))
                } else if (mainViewModel.whichAlarm.isOld && !switchState) {
                    mainViewModel.updateHandler(EventHandlerAlarm.getSnoozeTime(getSnoozeTime = -1))
                } else {
                    mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = -1))
                }
                controller.navigate(Routes.Preview.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }) { PV ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PV)
                .background(backColor)
        ) {
            Column {
                Card(
                    onClick = {},
                    modifier = Modifier
                        .height(62.dp)
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xff1C1F26)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Snooze",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = switchState,
                            onCheckedChange = { newSwitchState ->
                                switchState = newSwitchState
//                                updateAlarm(switchState)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xff12A8CB), // Color when switch is ON
                                checkedTrackColor = Color(0xff18677E), // Track color when switch is ON
                                uncheckedThumbColor = Color(0xffB5BCCB), // Color when switch is OFF
                                uncheckedTrackColor = Color(0xff111217) // Track color when switch is OFF
                            ), modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
            if (switchState) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        enabled = switchState,
                        shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xff1C1F26),
                            disabledContainerColor = Color(0xff16171B)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Snooze",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            listOfIntervals.forEach { item ->
                                Spacer(modifier = Modifier.height(12.dp))
                                SingleChoice(
                                    isSelected = item == selectedOption,
                                    onCLick = { selectedOption = item },
                                    title = item, enabled = switchState
                                )
                            }

                        }

                    }
                }
            }
        }
    }
}

@Composable
fun SingleChoice(isSelected: Boolean, onCLick: () -> Unit, title: String, enabled: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isSelected, onClick = { onCLick() }, enabled = enabled)
        Text(
            text = if(title!="Off") "$title minutes" else title,
            color = Color.White,
            fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp)
        )
    }
}