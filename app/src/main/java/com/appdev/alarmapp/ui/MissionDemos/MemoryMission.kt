package com.appdev.alarmapp.ui.MissionDemos

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import com.andyliu.compose_wheel_picker.VerticalWheelPicker
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.TopBar
import com.appdev.alarmapp.ui.PreivewScreen.getImageForSliderValue
import com.appdev.alarmapp.ui.PreivewScreen.getMathEqForSliderValue
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMissionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController
) {
    val itemsList by remember {
        mutableStateOf(listOf("Very Easy", "Easy", "Normal", "Hard"))
    }
    var selectedItem by remember {
        mutableStateOf(itemsList[0]) // initially, first item is selected
    }
    val context = LocalContext.current
    val state =
        rememberLazyListState(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Step")) (mainViewModel.missionDetails.repeatTimes / 10) - 1 else 0)

    var currentIndex by remember { mutableStateOf(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Step")) (mainViewModel.missionDetails.repeatTimes / 10) - 1 else 0) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = mainViewModel.missionDetails.repeatProgress) {
        if (mainViewModel.missionDetails.repeatProgress > 1) {
            mainViewModel.missionData(MissionDataHandler.MissionProgress(1))
        }
    }
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
                    border = BorderStroke(1.dp, Color.White),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(27.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = if (mainViewModel.whichMission.isMemory) "Memory" else if (mainViewModel.whichMission.isMath) "Math" else if (mainViewModel.whichMission.isShake) "Shake" else if (mainViewModel.whichMission.isSteps) "Step" else "",
                    color = Color.White,
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
                        .background(Color(0xff2F333E), shape = RoundedCornerShape(10.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    if (mainViewModel.whichMission.isMemory) {
                        Image(
                            painter = painterResource(id = getImageForSliderValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem)),
                            contentDescription = "", modifier = Modifier.size(170.dp)
                        )
                    } else if (mainViewModel.whichMission.isMath) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = getMathEqForSliderValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem),
                                color = Color.White,
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
                    } else if (mainViewModel.whichMission.isShake || mainViewModel.whichMission.isSteps) {
                        Image(
                            painter = painterResource(id = R.drawable.shakeimg),
                            contentDescription = "",
                            modifier = Modifier
                                .width(270.dp)
                                .height(180.dp)
                        )
                    }
                    if (!mainViewModel.whichMission.isShake && !mainViewModel.whichMission.isSteps) {
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
                                color = Color.White,
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
                        .background(Color(0xff2F333E), shape = RoundedCornerShape(10.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        VerticalWheelPicker(
                            state = state,
                            count = 3,
                            itemHeight = 40.dp,
                            visibleItemCount = 3,
                            onScrollFinish = { currentIndex = it }
                        ) { index ->
                            val isFocus = index == currentIndex
                            val targetAlpha = if (isFocus) 1.0f else 0.3f
                            val targetScale = if (isFocus) 1.0f else 0.8f
                            val animateScale by animateFloatAsState(targetScale)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
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
                                if (mainViewModel.whichMission.isShake) {
                                    mainViewModel.missionData(MissionDataHandler.RepeatTimes(repeat = (currentIndex + 1) * 5))
                                } else if (mainViewModel.whichMission.isSteps) {
                                    mainViewModel.missionData(MissionDataHandler.RepeatTimes(repeat = (currentIndex + 1) * 10))
                                } else {
                                    mainViewModel.missionData(MissionDataHandler.RepeatTimes(repeat = currentIndex + 1))
                                }
                                Text(
                                    text = if (mainViewModel.whichMission.isMath || mainViewModel.whichMission.isMemory) "${index + 1}" else if(mainViewModel.whichMission.isSteps) "${(index + 1) * 10}" else "${(index + 1) * 5}",
                                    color = if (index == currentIndex) Color.White else Color.Gray,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                        Text(
                            text = "times",
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(start = 15.dp)
                        )
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
//                                if (mainViewModel.whichMission.isMemory) {
//                                    controller.navigate(Routes.PreviewAlarm.route) {
//                                        popUpTo(controller.graph.startDestinationId)
//                                        launchSingleTop = true
//                                    }
//                                } else if (mainViewModel.whichMission.isMath) {
//                                    controller.navigate(Routes.PreviewAlarm.route) {
//                                        popUpTo(controller.graph.startDestinationId)
//                                        launchSingleTop = true
//                                    }
//                                } else if (mainViewModel.whichMission.isShake) {
//                                    controller.navigate(Routes.PreviewAlarm.route) {
//                                        popUpTo(controller.graph.startDestinationId)
//                                        launchSingleTop = true
//                                    }
//                                }
                                controller.navigate(Routes.PreviewAlarm.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            text = "Preview",
                            width = 0.3f,
                            backgroundColor = backColor,
                            isBorderPreview = true,
                            textColor = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        CustomButton(
                            onClick = {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(MissionDataHandler.SubmitData)
                                controller.navigate(Routes.MissionMenuScreen.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
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