package com.appdev.alarmapp.ui.MissionDemos

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.navigation.NavHostController
import com.andyliu.compose_wheel_picker.VerticalWheelPicker
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.getImageForSliderValue
import com.appdev.alarmapp.ui.PreivewScreen.getMathEqForSliderValue
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.motivationalPhrases
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingMissionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController
) {
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val context = LocalContext.current
    val state =
        rememberLazyListState(if ((mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) || (mainViewModel.missionDetails.missionName == "Typing")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else 0)
    var currentIndex by remember { mutableStateOf(if ((mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Math" || mainViewModel.missionDetails.missionName == "Memory")) || (mainViewModel.missionDetails.missionName == "Typing")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "Shake")) (mainViewModel.missionDetails.repeatTimes / 5) - 1 else 0) }
    var scope = rememberCoroutineScope()
    var randomSentence by remember {
        mutableStateOf(
            if (mainViewModel.missionDetails.selectedSentences.isNotEmpty()) convertStringToSet(
                mainViewModel.missionDetails.selectedSentences
            ).random() else mainViewModel.getRandomSentence()
        )
    }
    LaunchedEffect(key1 = currentIndex) {
        randomSentence =
            if (mainViewModel.missionDetails.selectedSentences.isNotEmpty()) convertStringToSet(
                mainViewModel.missionDetails.selectedSentences
            ).random() else mainViewModel.getRandomSentence()
    }
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
                    .fillMaxWidth(0.62f)
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
                    text = "Typing",
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
                    Card(
                        onClick = { /*TODO*/ },
                        enabled = false,
                        colors = CardDefaults.cardColors(disabledContainerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                    ) {
                        Text(
                            text = randomSentence.phraseData,
                            color = Color(0xffD66616),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W600,
                            modifier = Modifier.padding(25.dp)
                        )
                    }
                    if (!mainViewModel.whichMission.isShake) {
                        Text(
                            text = "Example",
                            color = Color(0xffD66616),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
                        )
                    }

                }
                //COUNTER

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 22.dp,
                            vertical = 5.dp
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
                            count = 5,
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
                                mainViewModel.missionData(MissionDataHandler.RepeatTimes(repeat = currentIndex + 1))

                                Text(
                                    text = "${index + 1}",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp)
                        .clickable {

                            controller.navigate(Routes.SentenceScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        .background(Color(0xff2F333E), shape = RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select the sentences",
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 15.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForwardIos,
                                contentDescription = "",
                                tint = Color.White.copy(
                                    alpha = 0.8f
                                ),
                                modifier = Modifier.size(15.dp)
                            )
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
                                if (!mainViewModel.isRealAlarm) {
                                    Helper.playStream(context, R.raw.alarmsound)
                                }
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
                                if (mainViewModel.managingDefault) {
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            isSelected = true
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.SelectedSentences(
                                            mainViewModel.sentencesList
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
                                    controller.navigate(Routes.MissionMenuScreen.route) {
                                        popUpTo(controller.graph.startDestinationId)
                                        launchSingleTop = true
                                    }

                                } else {
                                    mainViewModel.missionData(
                                        MissionDataHandler.IsSelectedMission(
                                            isSelected = true
                                        )
                                    )
                                    mainViewModel.missionData(
                                        MissionDataHandler.SelectedSentences(
                                            mainViewModel.sentencesList
                                        )
                                    )

                                    mainViewModel.missionData(MissionDataHandler.SubmitData)
                                    controller.navigate(Routes.MissionMenuScreen.route) {
                                        popUpTo(controller.graph.startDestinationId)
                                        launchSingleTop = true
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