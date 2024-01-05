package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.SingleOption
import com.appdev.alarmapp.ui.PreivewScreen.singleMission
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.ringtoneList
import com.appdev.alarmapp.utils.whichMissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAlarmMissions(controller: NavHostController, mainViewModel: MainViewModel) {
    val scrollStateRow = rememberScrollState()
    val stateFlowObject = mainViewModel.defaultSettings.collectAsStateWithLifecycle()

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
                    .fillMaxWidth(0.90f)
                    .padding(vertical = 10.dp, horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        mainViewModel.setDefaultSettings(
                            DefaultSettingsHandler.GoingToSetDefault(
                                false
                            )
                        )
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
                    text = "Default Setting for New Alarms",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 10.dp)
                    .background(elementBack, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 23.dp, horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Mission",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = stateFlowObject.value.listOfMissions.size.toString()+ "/5",
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
                        val emptyStaticSlots =
                            Integer.max(5 - stateFlowObject.value.listOfMissions.size, 0)

                        // Display dynamic slots based on the list of missions

                            stateFlowObject.value.listOfMissions.forEach { mission ->
                                singleMission(mission, remove = { md ->
                                    val newList =
                                        mainViewModel.defaultSettings.value.listOfMissions.toMutableList()
                                    newList.remove(md)
                                    mainViewModel.setDefaultSettings(
                                        DefaultSettingsHandler.GetNewObject(
                                            defaultSettings = DefaultSettings(
                                                id = mainViewModel.defaultSettings.value.id,
                                                ringtone = mainViewModel.defaultSettings.value.ringtone,
                                                snoozeTime = mainViewModel.defaultSettings.value.snoozeTime,
                                                listOfMissions = newList
                                            )
                                        )
                                    )
                                    mainViewModel.setDefaultSettings(DefaultSettingsHandler.UpdateDefault)

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
                                            mainViewModel.whichMissionHandle(
                                                whichMissionHandler.thisMission(
                                                    missionMemory = true,
                                                    missionMath = false,
                                                    missionShake = false,
                                                    isSteps = false
                                                )
                                            )
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
                                            mainViewModel.whichMissionHandle(
                                                whichMissionHandler.thisMission(
                                                    missionMemory = false,
                                                    missionMath = false,
                                                    missionShake = true,
                                                    isSteps = false
                                                )
                                            )
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
                                            mainViewModel.whichMissionHandle(
                                                whichMissionHandler.thisMission(
                                                    missionMemory = false,
                                                    missionMath = true,
                                                    missionShake = false,
                                                    isSteps = false
                                                )
                                            )
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
                                                popUpTo(controller.graph.startDestinationId)
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
                                                popUpTo(controller.graph.startDestinationId)
                                                launchSingleTop = true
                                            }

                                        }

                                        else -> {}
                                    }
                                }) {
                                    controller.navigate(Routes.MissionMenuScreen.route) {
                                        popUpTo(Routes.DefaultSettingsScreen.route) {
                                            inclusive = false
                                        }
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
                    .padding(top = 8.dp, start = 10.dp)
                    .background(elementBack, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                DefaultSingleOption(
                    title = "Sound",
                    data =  stateFlowObject.value.ringtone.name
                ) {
                    controller.navigate(Routes.Ringtone.route) {
                        popUpTo(Routes.DefaultSettingsScreen.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 10.dp)
                    .background(elementBack, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                DefaultSingleOption(
                    title = "Snooze",
                    data = if (stateFlowObject.value.snoozeTime != -1) "${stateFlowObject.value.snoozeTime} min" else "off"

                ) {
                    controller.navigate(Routes.SnoozeScreen.route) {
                        popUpTo(Routes.DefaultSettingsScreen.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }

        }
    }
}

@Composable
fun DefaultSingleOption(
    title: String,
    data: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                textAlign = TextAlign.Start, fontSize = 16.sp
            )
            Text(
                data.trim(), fontSize = 15.sp,
                letterSpacing = 0.sp,
                color = signatureBlue, modifier = Modifier.padding(top = 4.dp)
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = { onClick() }) {
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