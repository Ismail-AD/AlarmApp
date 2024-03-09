package com.appdev.alarmapp.ui.StartingScreens.DemoScreens

import android.util.Log
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
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.scheduleTheAlarm
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.newAlarmHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternPick(controller: NavHostController, mainViewModel: MainViewModel) {
    var selectedCard by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val alarmScheduler by remember {
        mutableStateOf(AlarmScheduler(context, mainViewModel))
    }
    var remainingTime by remember { mutableStateOf(0L) }
    val alarmIdRec by mainViewModel.alarmID.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = alarmIdRec) {
        if (alarmIdRec != 0L) {
            mainViewModel.insertMissions(
                missionsEntity(
                    alarmIdRec,
                    mainViewModel.missionDetailsList
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.15f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose the wake-up mission you prefer",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold
            )
        }

        MemoryBox(selectedCard == 0, iconID = Icons.Filled.Calculate, title = "Math") {
            selectedCard = 0
            showBottomSheet = true
        }
        MemoryBox(selectedCard == 1, iconID = Icons.Filled.AutoAwesomeMosaic, title = "Memory") {
            selectedCard = 1
            showBottomSheet = true
        }
        MemoryBox(selectedCard == 2, iconID = Icons.Filled.ScreenRotation, title = "Shake") {
            selectedCard = 2
            showBottomSheet = true
        }
        MemoryBox(selectedCard == 3, iconID = Icons.Filled.Close, title = "Off") {
            selectedCard = 3
        }
        if (selectedCard != -1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomButton(onClick = {
                    if (selectedCard != 3) {
                        mainViewModel.missionData(
                            MissionDataHandler.MissionProgress(
                                repeatProgress = 1
                            )
                        )
                        mainViewModel.missionData(
                            MissionDataHandler.IsSelectedMission(
                                true
                            )
                        )
                        mainViewModel.missionData(
                            MissionDataHandler.MissionLevel(
                                missionLevel = "Very Easy"
                            )
                        )
                        when (selectedCard) {
                            0 -> {
                                mainViewModel.missionData(
                                    MissionDataHandler.MissionName(
                                        missionName = "Math"
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.RepeatTimes(
                                        repeat = 1
                                    )
                                )
                            }

                            1 -> {
                                mainViewModel.missionData(
                                    MissionDataHandler.MissionName(
                                        missionName = "Memory"
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.RepeatTimes(
                                        repeat = 1
                                    )
                                )
                            }

                            2 -> {
                                mainViewModel.missionData(
                                    MissionDataHandler.MissionName(
                                        missionName = "Shake"
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.RepeatTimes(
                                        repeat = 5
                                    )
                                )
                            }
                        }

                        mainViewModel.missionData(MissionDataHandler.SubmitData)
                    }
                    mainViewModel.newAlarmHandler(newAlarmHandler.isOneTime(true))
                    mainViewModel.newAlarmHandler(newAlarmHandler.getMissions(missions = mainViewModel.missionDetailsList))
                    scheduleTheAlarm(
                        mainViewModel,
                        false,
                        mainViewModel.newAlarm,
                        alarmScheduler,
                        mainViewModel.whichAlarm.isOld,
                    ) { tomorrowTimeMillis, currentTimeMillis ->
                        remainingTime = tomorrowTimeMillis - currentTimeMillis
                    }
                    Log.d("CHKIT", mainViewModel.newAlarm.ringtone.rawResourceId.toString())
                    mainViewModel.newAlarmHandler(newAlarmHandler.insert)
                    controller.navigate(Routes.Setting.route) {
                        launchSingleTop = true
                        popUpTo(Routes.Pattern.route) {
                            inclusive = true
                        }
                    }
                }, text = "Next")
            }
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = {}) {
            Column(modifier = Modifier.background(Color(0xff1C1F26))) {
                when (selectedCard) {
                    0 -> {
                        singleSheetItem(
                            title = "Math",
                            iconInTitle = Icons.Filled.Calculate,
                            percentage = "60",
                            fill = 0.6f,
                            percentage2 = "90",
                            fill2 = 0.9f
                        ) {
                            scope.launch {
                                sheetState.hide()
                            }
                            showBottomSheet = false
                        }
                    }

                    1 -> {
                        singleSheetItem(
                            title = "Memory",
                            iconInTitle = Icons.Filled.AutoAwesomeMosaic,
                            percentage = "75",
                            fill = 0.75f,
                            percentage2 = "75",
                            fill2 = 0.75f
                        ) {
                            scope.launch {
                                sheetState.hide()
                            }
                            showBottomSheet = false
                        }
                    }

                    2 -> {
                        singleSheetItem(
                            title = "Shake",
                            iconInTitle = Icons.Filled.ScreenRotation,
                            percentage = "90",
                            fill = 0.9f,
                            percentage2 = "60",
                            fill2 = 0.6f
                        ) {
                            scope.launch {
                                sheetState.hide()
                            }
                            showBottomSheet = false
                        }
                    }

                    else -> {
                        // Handle unexpected card value
                    }
                }
            }
        }
    }
}

@Composable
fun singleSheetItem(
    title: String,
    iconInTitle: ImageVector,
    percentage: String,
    fill: Float,
    percentage2: String,
    fill2: Float,
    CloseIt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { CloseIt() }) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "", tint = Color.White)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = iconInTitle, contentDescription = "", tint = Color.White)
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 7.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.mobileinhand),
                contentDescription = "", modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        belowImage(
            title = "Energy Boost",
            iconID = R.drawable.workout,
            percentage = percentage,
            fill = fill,
            backgroundColor = Color(0xffA88B37)
        )
        Spacer(modifier = Modifier.height(15.dp))
        belowImage(
            title = "Brain Stimulation",
            iconID = R.drawable.brain,
            percentage = percentage2,
            fill = fill2,
            backgroundColor = Color(0xff4DD1F2)
        )
    }
}

@Composable
fun belowImage(
    title: String,
    iconID: Int,
    percentage: String,
    fill: Float,
    backgroundColor: Color
) {
    Column(modifier = Modifier.padding(top = 10.dp, start = 35.dp, end = 35.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconID),
                contentDescription = "",
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "$percentage%",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Color(0xff282B34))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .padding(horizontal = 8.dp)
            ) {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryBox(isSelected: Boolean, iconID: ImageVector, title: String, onClick: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
            onClick = {
                onClick()
            },
            modifier = Modifier
                .height(62.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
            border = BorderStroke(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xff0FAACB) else Color.Transparent
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xff2F333E) else Color(0xff24272E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = iconID, contentDescription = "", tint = Color.White)
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                )
            }
        }
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        PatternPick(controller = rememberNavController())
//    }
//}