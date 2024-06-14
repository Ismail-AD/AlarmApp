package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainScreen.getAMPM
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.Snooze.SingleChoice
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.listOfIntervals
import com.appdev.alarmapp.utils.listOfMissionTime
import com.appdev.alarmapp.utils.listOfSensi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDismissSettings(mainViewModel: MainViewModel, controller: NavHostController) {

    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val dismissSettings = mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    var switchState by remember { mutableStateOf(dismissSettings.value.muteTone) }
    var showDismissSheet by remember { mutableStateOf(false) }
    var showLimitSheet by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(if (dismissSettings.value.dismissTime == 0) "Off" else dismissSettings.value.dismissTime.toString()) }
    var selectedOptionLimit by remember { mutableStateOf(dismissSettings.value.missionTime.toString()) }
    var selectedOptionSensi by remember { mutableStateOf(dismissSettings.value.photoSensitivity) }



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
                    .fillMaxWidth(0.79f)
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
                    text = "Dismiss Alarm/Mission",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Text(
                "Alarm Dismissal",
                fontSize = 15.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp)
            )

            DismissOption(
                title = "Auto Dismiss",
                data = selectedOption + if (selectedOption != "Off") " minute" else ""
            ) {
                showDismissSheet = true
            }

            Text(
                "Turn off alarm if unresponsive for a certain amount of time",
                fontSize = 12.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 7.dp)
            )
            Text(
                "Mission Dismissal",
                fontSize = 15.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 15.dp)
            )
            DismissOption(
                title = "Mission time limit",
                data = "$selectedOptionLimit seconds"
            ) {
                showLimitSheet = true
            }

            Column {
                Spacer(modifier = Modifier.height(9.dp))
                Card(
                    onClick = {},
                    modifier = Modifier
                        .height(68.dp)
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed ,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Mute during mission",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Switch(
                                checked = switchState,
                                onCheckedChange = { newSwitchState ->
                                    switchState = newSwitchState
                                    mainViewModel.updateDismissSettings(
                                        DismissSettings(
                                            id = mainViewModel.dismissSettings.value.id,
                                            dismissTime = mainViewModel.dismissSettings.value.dismissTime,
                                            missionTime = mainViewModel.dismissSettings.value.missionTime,
                                            photoSensitivity = mainViewModel.dismissSettings.value.photoSensitivity,
                                            muteTone = switchState
                                        )
                                    )
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
            DismissOption(
                title = "Photo Sensitivity",
                data = selectedOptionSensi
            ) {
                showPhotoSheet = true
            }

        }
        if (showDismissSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                    showDismissSheet = false
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(vertical = 16.dp, horizontal = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto Dismiss",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            SingleAttempt(
                                isSelected = "Off" == selectedOption,
                                onCLick = {
                                    selectedOption = "Off"
                                    showDismissSheet = false
                                    mainViewModel.updateDismissSettings(
                                        DismissSettings(
                                            id = mainViewModel.dismissSettings.value.id,
                                            dismissTime = 0,
                                            missionTime = mainViewModel.dismissSettings.value.missionTime,
                                            photoSensitivity = mainViewModel.dismissSettings.value.photoSensitivity,
                                            muteTone = mainViewModel.dismissSettings.value.muteTone
                                        )
                                    )
                                },
                                title = "Off"
                            )
                            listOfIntervals.forEach { item ->
                                Spacer(modifier = Modifier.height(7.dp))
                                SingleAttempt(
                                    isSelected = item == selectedOption,
                                    onCLick = {
                                        selectedOption = item
                                        showDismissSheet = false
                                        mainViewModel.updateDismissSettings(
                                            DismissSettings(
                                                id = mainViewModel.dismissSettings.value.id,
                                                dismissTime = selectedOption.trim().toInt(),
                                                missionTime = mainViewModel.dismissSettings.value.missionTime,
                                                photoSensitivity = mainViewModel.dismissSettings.value.photoSensitivity,
                                                muteTone = mainViewModel.dismissSettings.value.muteTone
                                            )
                                        )
                                    },
                                    title = item
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
        if (showLimitSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                    showLimitSheet = false
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(vertical = 16.dp, horizontal = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mission time limit",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            listOfMissionTime.forEach { item ->
                                Spacer(modifier = Modifier.height(7.dp))
                                SingleAttemptMissionLimit(
                                    isSelected = item == selectedOptionLimit,
                                    onCLick = {
                                        selectedOptionLimit = item
                                        showLimitSheet = false
                                        mainViewModel.updateDismissSettings(
                                            DismissSettings(
                                                id = mainViewModel.dismissSettings.value.id,
                                                dismissTime = mainViewModel.dismissSettings.value.dismissTime,
                                                missionTime = selectedOptionLimit.toInt(),
                                                photoSensitivity = mainViewModel.dismissSettings.value.photoSensitivity,
                                                muteTone = mainViewModel.dismissSettings.value.muteTone
                                            )
                                        )
                                    },
                                    title = item
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
        if (showPhotoSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                    showPhotoSheet = false
                }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 16.dp, horizontal = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photo sensitivity",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            listOfSensi.forEach { item ->
                                Spacer(modifier = Modifier.height(7.dp))
                                SingleAttemptSenstivity(
                                    isSelected = item == selectedOptionSensi,
                                    onCLick = {
                                        selectedOptionSensi = item
                                        showPhotoSheet = false
                                        mainViewModel.updateDismissSettings(
                                            DismissSettings(
                                                id = mainViewModel.dismissSettings.value.id,
                                                dismissTime = mainViewModel.dismissSettings.value.dismissTime,
                                                missionTime = mainViewModel.dismissSettings.value.missionTime,
                                                photoSensitivity = selectedOptionSensi,
                                                muteTone = mainViewModel.dismissSettings.value.muteTone
                                            )
                                        )
                                    },
                                    title = item
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissOption(
    title: String,
    data: String,
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(9.dp))
    Card(
        onClick = {
            onClick()
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
}

@Composable
fun SingleAttempt(isSelected: Boolean, onCLick: () -> Unit, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = isSelected, onClick = { onCLick() },colors = RadioButtonDefaults.colors(selectedColor = Color(0xff18677E), unselectedColor = Color(0xffB6BDCA)))
        Text(
            text = if (title != "Off") "$title minutes" else title,
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 17.sp, modifier = Modifier.padding(start = 6.dp)
        )
    }
}
@Composable
fun SingleAttemptMissionLimit(isSelected: Boolean, onCLick: () -> Unit, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = isSelected, onClick = { onCLick() },colors = RadioButtonDefaults.colors(selectedColor = Color(0xff18677E), unselectedColor = Color(0xffB6BDCA)))
        Text(
            text = if (title != "Off") "$title seconds" else title,
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 17.sp, modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun SingleAttemptSenstivity(isSelected: Boolean, onCLick: () -> Unit, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = isSelected, onClick = { onCLick() },colors = RadioButtonDefaults.colors(selectedColor = Color(0xff18677E), unselectedColor = Color(0xffB6BDCA)))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 17.sp, modifier = Modifier.padding(start = 6.dp)
        )
    }
}