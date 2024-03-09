package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.checkOutViewModel
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
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAlarmMissions(
    controller: NavHostController,
    mainViewModel: MainViewModel,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val scrollStateRow = rememberScrollState()
    val stateFlowObject = mainViewModel.defaultSettings.collectAsStateWithLifecycle()
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()

    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var currentState by remember { mutableStateOf(billingState.value) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
        loading = false
    }
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Log.d("CHKMD","AT VIEW OF DEFAULT ${mainViewModel.managingDefault}")

        if (loading) {
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
                .background(MaterialTheme.colorScheme.background)
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
                        controller.navigate(Routes.SettingsOfAlarmScreen.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
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
                    text = "Default Setting for New Alarms",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 10.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
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
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text =  stateFlowObject.value.listOfMissions.size.toString() + "/5",
                            color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.7f),
                            fontSize = 15.sp
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
                            singleMission(isLock = false, mission, remove = { md ->
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
                                                isSteps = false, isSquat = false
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
                                                isSteps = false, isSquat = false
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
                                                isSteps = false, isSquat = false
                                            )
                                        )
                                        controller.navigate(Routes.CommonMissionScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                                                isSteps = true, isSquat = false
                                            )
                                        )
                                        controller.navigate(Routes.CommonMissionScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
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
                                                isSteps = false, isSquat = true
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
                        if (currentState is BillingResultState.Success) {
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
                        } else {
                            if (stateFlowObject.value.listOfMissions.isEmpty()) {
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
                            repeat(emptyStaticSlots) {
                                singleMission(isLock = true,
                                    Missions(),
                                    remove = {},
                                    moveToDetails = {}) {
                                    controller.navigate(Routes.Purchase.route) {
                                        popUpTo(Routes.DefaultSettingsScreen.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop=true
                                    }
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
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                DefaultSingleOption(
                    title = "Sound",
                    data = stateFlowObject.value.ringtone.name
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
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp)),
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
                color = MaterialTheme.colorScheme.surfaceTint,
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
                    tint = MaterialTheme.colorScheme.surfaceTint.copy(
                        alpha = 0.6f
                    ),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}