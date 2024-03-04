package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.andyliu.compose_wheel_picker.HorizontalWheelPicker
import com.andyliu.compose_wheel_picker.VerticalWheelPicker
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.TopBar
import com.appdev.alarmapp.ui.PreivewScreen.getImageForSliderValue
import com.appdev.alarmapp.ui.PreivewScreen.getMathEqForSliderValue
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MemoryMissionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController,checkOutViewModel: checkOutViewModel = hiltViewModel()
) {

    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }
    var loading by remember { mutableStateOf(true) }
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val itemsList by remember {
        mutableStateOf(listOf("Very Easy", "Easy", "Normal", "Hard"))
    }
    var selectedItem by remember {
        mutableStateOf(mainViewModel.missionDetails.missionLevel) // initially, first item is selected
    }
    val context = LocalContext.current
    val state =
        rememberLazyListState(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Step")) (mainViewModel.missionDetails.repeatTimes / 10) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Squat")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else 0)

    var currentIndex by remember { mutableStateOf(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Step")) (mainViewModel.missionDetails.repeatTimes / 10) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Squat")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else 0) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = mainViewModel.missionDetails.repeatProgress) {
        if (mainViewModel.missionDetails.repeatProgress > 1) {
            mainViewModel.missionData(MissionDataHandler.MissionProgress(1))
        }
    }
    BackHandler {
        controller.navigate(Routes.MissionMenuScreen.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }
    LaunchedEffect(key1 = billingState.value){
        currentState = billingState.value
        loading=false
    }
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
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
                    .fillMaxWidth(if (mainViewModel.whichMission.isMemory) 0.64f else 0.6f)
                    .padding(vertical = 10.dp, horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        controller.navigate(Routes.MissionMenuScreen.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(27.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }

                Text(
                    text = if (mainViewModel.whichMission.isMemory) "Memory" else if (mainViewModel.whichMission.isMath) "Math" else if (mainViewModel.whichMission.isShake) "Shake" else if (mainViewModel.whichMission.isSteps) "Step" else if (mainViewModel.whichMission.isSquat) "Squat" else "",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 22.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    if (mainViewModel.whichMission.isMemory) {
                        Image(
                            painter = painterResource(id = getImageForSliderValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem)),
                            contentDescription = "", modifier = Modifier.size(150.dp)
                        )
                    } else if (mainViewModel.whichMission.isMath) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = getMathEqForSliderValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem),
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 30.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(end = 8.dp),
                                fontWeight = FontWeight.W600
                            )

                            Card(
                                onClick = { },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(60.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = (1.5).dp,
                                    color = Color(0xffA6ACB5)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .fillMaxSize(), contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QuestionMark,
                                        contentDescription = "",
                                        tint = Color(0xffA6ACB5),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    } else if (mainViewModel.whichMission.isSquat || mainViewModel.whichMission.isShake || mainViewModel.whichMission.isSteps) {
                        Image(
                            painter = painterResource(id = R.drawable.shakeimg),
                            contentDescription = "",
                            modifier = Modifier
                                .width(270.dp)
                                .height(180.dp)
                        )
                    }
                    if (!mainViewModel.whichMission.isShake && !mainViewModel.whichMission.isSteps && !mainViewModel.whichMission.isSquat) {
                        Text(
                            text = "Example",
                            color = Color(0xffD66616),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(top = if (mainViewModel.whichMission.isMath) 30.dp else 16.dp)
                        )
                    }
                    if (mainViewModel.whichMission.isMath || mainViewModel.whichMission.isMemory) {

                        Column(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 10.dp)
                                .background(Color.Transparent, RoundedCornerShape(10.dp)),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = if (mainViewModel.whichMission.isMemory) "Memory Level" else if (mainViewModel.whichMission.isMath) "Math Level" else if (mainViewModel.whichMission.isShake) "Shake Level" else "",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                            LazyColumn {
                                items(itemsList.chunked(3)) { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            FilterChip(
                                                selected = (item == selectedItem),
                                                onClick = {
                                                    selectedItem = item
                                                    mainViewModel.missionData(
                                                        MissionDataHandler.MissionLevel(
                                                            selectedItem
                                                        )
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = Color.Transparent,
                                                    labelColor = Color.Gray,
                                                    selectedContainerColor = Color(0xff7B70FF),
                                                    selectedLabelColor = Color.White
                                                ),
                                                label = {
                                                    Text(text = item)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                //COUNTER

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 22.dp,
                            vertical = if (mainViewModel.whichMission.isShake) 8.dp else 5.dp
                        )
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Text(
                            text = "Times count",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp, bottom = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalWheelPicker(
                                state = state,
                                count = 5,
                                itemWidth = 70.dp,
                                visibleItemCount = 3,
                                onScrollFinish = { currentIndex = it }
                            ) { index ->
                                val isFocus = index == currentIndex
                                val targetAlpha = if (isFocus) 1.0f else 0.3f
                                val targetScale = if (isFocus) 1.0f else 0.8f
                                val animateScale by animateFloatAsState(targetScale)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            this.alpha = targetAlpha
                                            this.scaleX = animateScale
                                            this.scaleY = animateScale
                                        }
                                        .clickable {
                                            scope.launch {
                                                state.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (mainViewModel.whichMission.isShake || mainViewModel.whichMission.isSquat) {
                                        mainViewModel.missionData(
                                            MissionDataHandler.RepeatTimes(
                                                repeat = (currentIndex + 1) * 5
                                            )
                                        )
                                    } else if (mainViewModel.whichMission.isSteps) {
                                        mainViewModel.missionData(
                                            MissionDataHandler.RepeatTimes(
                                                repeat = (currentIndex + 1) * 10
                                            )
                                        )
                                    } else {
                                        mainViewModel.missionData(
                                            MissionDataHandler.RepeatTimes(
                                                repeat = currentIndex + 1
                                            )
                                        )
                                    }
                                    Card(
                                        onClick = {
                                            scope.launch {
                                                state.animateScrollToItem(index)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(70.dp)
                                            .width(250.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        ),
                                        border = BorderStroke(width = 2.dp, color = Color(0xffA6ACB5))
                                    ){
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                                            Text(
                                                text = if (mainViewModel.whichMission.isMath || mainViewModel.whichMission.isMemory) "${index + 1}" else if (mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) "${(index + 1) * 5}" else "${(index + 1) * 5}",
                                                color = if (isDarkMode) if (index == currentIndex) Color.White else Color.Gray else if (index == currentIndex) Color.Black else Color.Gray,
                                                fontSize = 27.sp,
                                                fontWeight = FontWeight.W600
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        CustomButton(
                            onClick = {
                                Helper.playStream(context, R.raw.alarmsound)
                                controller.navigate(Routes.PreviewAlarm.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            text = "Preview",
                            width = 0.3f,
                            backgroundColor = MaterialTheme.colorScheme.background,
                            isBorderPreview = true,
                            textColor = if (isDarkMode) Color.LightGray else Color.Black
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        CustomButton(
                            isLock = (mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) && currentState !is BillingResultState.Success,
                            onClick = {
                                Log.d("CHKMD","AT BAR CODE DS ${mainViewModel.managingDefault}")
                                if((mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) && currentState !is BillingResultState.Success){
                                    controller.navigate(Routes.Purchase.route) {
                                        popUpTo(Routes.CommonMissionScreen.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else{
                                    if (mainViewModel.managingDefault) {
                                        mainViewModel.missionData(
                                            MissionDataHandler.IsSelectedMission(
                                                isSelected = true
                                            )
                                        )
                                        mainViewModel.missionData(MissionDataHandler.SubmitData)
                                        mainViewModel.setDefaultSettings(
                                            DefaultSettingsHandler.GetNewObject(
                                                defaultSettings = DefaultSettings(
                                                    id = mainViewModel.defaultSettings.value.id,
                                                    ringtone = mainViewModel.defaultSettings.value.ringtone,
                                                    snoozeTime = mainViewModel.defaultSettings.value.snoozeTime,
                                                    listOfMissions = mainViewModel.missionDetailsList
                                                )
                                            )
                                        )
                                        mainViewModel.setDefaultSettings(DefaultSettingsHandler.UpdateDefault)
                                        controller.navigate(Routes.DefaultSettingsScreen.route) {
                                            popUpTo(Routes.SettingsOfAlarmScreen.route) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        mainViewModel.missionData(
                                            MissionDataHandler.IsSelectedMission(
                                                isSelected = true
                                            )
                                        )
                                        mainViewModel.missionData(MissionDataHandler.SubmitData)
                                        controller.navigate(Routes.Preview.route) {
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                }

                            },
                            text = "Complete",
                            width = 0.75f,
                            backgroundColor = Color(0xff7B70FF),
                            textColor = Color.White
                        )
                    }
                }

            }
        }
    }
}