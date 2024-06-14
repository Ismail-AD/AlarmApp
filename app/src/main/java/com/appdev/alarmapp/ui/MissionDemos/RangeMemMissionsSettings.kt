package com.appdev.alarmapp.ui.MissionDemos

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.PreivewScreen.getAlphaImageForSelectedValue
import com.appdev.alarmapp.ui.PreivewScreen.getImageForSelectedValue
import com.appdev.alarmapp.ui.PreivewScreen.getImageForSliderValue
import com.appdev.alarmapp.ui.PreivewScreen.getMathEqForSliderValue
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.MissionDataHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RangeMemoryMissionSetting(
    mainViewModel: MainViewModel,
    controller: NavHostController, checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }
    var loading by remember { mutableStateOf(true) }

    val itemsList by remember {
        mutableStateOf(listOf("Normal Mode", "Hard Mode"))
    }
    var selectedItem by remember {
        mutableStateOf(mainViewModel.missionDetails.difficultyLevel) // initially, first item is selected
    }
    val context = LocalContext.current
    val timesState =
        rememberLazyListState(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "RangeNumbers" || mainViewModel.missionDetails.missionName == "RangeAlphabet" || mainViewModel.missionDetails.missionName == "ArrangeShapes" || mainViewModel.missionDetails.missionName == "ArrangeAlphabet" || mainViewModel.missionDetails.missionName == "ArrangeNumbers")) mainViewModel.missionDetails.repeatTimes - 1 else 0)
    val rangeState_312 =
        rememberLazyListState(if (mainViewModel.missionDetails.isSelected) mainViewModel.missionDetails.valuesToPick - 3 else 1)

    var currentIndexTimes by remember { mutableStateOf(if (mainViewModel.missionDetails.isSelected && (mainViewModel.missionDetails.missionName == "RangeNumbers")) mainViewModel.missionDetails.repeatTimes - 1 else if (mainViewModel.missionDetails.isSelected && mainViewModel.missionDetails.missionName == "RangeAlphabet") mainViewModel.missionDetails.repeatTimes - 1 else 0) }
    var currentIndexRange_312 by remember { mutableStateOf(if (mainViewModel.missionDetails.isSelected) (mainViewModel.missionDetails.valuesToPick - 3) else 1) }

    val scope = rememberCoroutineScope()


    val defaultSettings = mainViewModel.defaultSettings.collectAsStateWithLifecycle()


    LaunchedEffect(key1 = mainViewModel.missionDetails.repeatProgress) {
        if (mainViewModel.missionDetails.repeatProgress > 1) {
            mainViewModel.missionData(MissionDataHandler.MissionProgress(1))
        }
    }
    BackHandler {
        controller.popBackStack()
    }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
        loading = false
    }
    val scrollState = rememberScrollState()

    //---------------CODE---------------------------

    Scaffold(bottomBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                CustomButton(
                    onClick = {
                        controller.navigate(Routes.PreviewAlarm.route) {
                            popUpTo(Routes.CommonMissionScreen.route) {
                                inclusive = false
                            }
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
//                        (mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) && currentState !is BillingResultState.Success
                CustomButton(
                    isLock = (mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) && currentState is BillingResultState.Success,
                    onClick = {
//                                if ((mainViewModel.whichMission.isSteps || mainViewModel.whichMission.isSquat) && currentState !is BillingResultState.Success) {
//                                    controller.navigate(Routes.Purchase.route) {
//                                        popUpTo(Routes.CommonMissionScreen.route) {
//                                            inclusive = false
//                                        }
//                                        launchSingleTop = true
//                                    }
//                                } else {
                        if (mainViewModel.managingDefault) {
                            mainViewModel.missionData(
                                MissionDataHandler.IsSelectedMission(
                                    isSelected = true
                                )
                            )
//                                        if(currentState is BillingResultState.Success){
                            mainViewModel.missionData(
                                MissionDataHandler.AddList(
                                    defaultSettings.value.listOfMissions
                                )
                            )
//                                        }
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
//                                        if (currentState !is BillingResultState.Success) {
//                                            mainViewModel.missionData(MissionDataHandler.ResetList)
//                                        }
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
//                                }

                    },
                    text = "Complete",
                    width = 0.75f,
                    backgroundColor = Color(0xff7B70FF),
                    textColor = Color.White
                )
            }
        }
    }, topBar = {
        TopAppBar(title = {
            Box(modifier = Modifier.fillMaxWidth(0.85f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (mainViewModel.whichRangeMission.isNumbers) "Numbers" else if (mainViewModel.whichRangeMission.isAlphabets) "Alphabets"  else if (mainViewModel.whichRangeMission.orderAlphabet) "Arrange Alphabets" else if (mainViewModel.whichRangeMission.orderNumbers) "Arrange Numbers" else if (mainViewModel.whichRangeMission.orderShapes) "Arrange Shapes" else "",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 17.sp, fontWeight = FontWeight.W500
                )
            }
        }, navigationIcon = {
            Card(
                onClick = {
                    controller.popBackStack()
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
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isDarkMode) backColor else MaterialTheme.colorScheme.background,
            navigationIconContentColor = Color.White
        )
        )
    }) {
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
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 22.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    if(mainViewModel.whichRangeMission.isNumbers){
                        Image(
                            painter = painterResource(id = getImageForSelectedValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem)),
                            contentDescription = "", modifier = Modifier.size(200.dp)
                        )
                    } else if( mainViewModel.whichRangeMission.orderNumbers){
                        Image(
                            painter = painterResource(id = R.drawable.arrangenum),
                            contentDescription = "", modifier = Modifier.size(200.dp)
                        )
                    } else if( mainViewModel.whichRangeMission.orderAlphabet){
                        Image(
                            painter = painterResource(id = R.drawable.arrangealpha),
                            contentDescription = "", modifier = Modifier.size(200.dp)
                        )
                    }
                    else if( mainViewModel.whichRangeMission.isAlphabets){
                        Image(
                            painter = painterResource(id = getAlphaImageForSelectedValue(if (mainViewModel.missionDetails.missionID > 1) mainViewModel.missionDetails.missionLevel else selectedItem)),
                            contentDescription = "", modifier = Modifier.size(200.dp)
                        )
                    } else{
                        Image(
                            painter = painterResource(id = R.drawable.shapeset),
                            contentDescription = "", modifier = Modifier.size(200.dp)
                        )
                    }
//                    }

                    Text(
                        text = "Example",
                        color = Color(0xffD66616),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier.padding(top = if (mainViewModel.whichMission.isMath) 30.dp else 16.dp, bottom = 10.dp)
                    )
                    if(mainViewModel.missionDetails.missionName != "ArrangeNumbers" && mainViewModel.missionDetails.missionName != "ArrangeAlphabet" && mainViewModel.missionDetails.missionName != "ArrangeShapes"){
                        Column(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 10.dp)
                                .background(Color.Transparent, RoundedCornerShape(10.dp)),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Difficulty Level",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                itemsList.forEach { item ->
                                    FilterChip(
                                        selected = (item == selectedItem),
                                        onClick = {
                                            selectedItem = item
                                            mainViewModel.missionData(
                                                MissionDataHandler.DifficultyLevel(
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
                            text = if (mainViewModel.whichRangeMission.isNumbers) "Numbers count" else "Alphabets count",
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
                                state = rangeState_312,
                                count = 10,
                                itemWidth = 70.dp,
                                visibleItemCount = 3,
                                onScrollFinish = { currentIndexRange_312 = it }
                            ) { index ->
                                val isFocus = index == currentIndexRange_312
                                val targetAlpha = if (isFocus) 1.0f else 0.3f
                                val targetScale = if (isFocus) 1.0f else 0.8f
                                val animateScale by animateFloatAsState(targetScale)
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .graphicsLayer {
                                            this.alpha = targetAlpha
                                            this.scaleX = animateScale
                                            this.scaleY = animateScale
                                        }
                                        .clickable {
                                            scope.launch {
                                                rangeState_312.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    mainViewModel.missionData(
                                        MissionDataHandler.NumbersCount(
                                            noOfValue = currentIndexRange_312 + 3
                                        )
                                    )
                                    Card(
                                        onClick = {
                                            scope.launch {
                                                rangeState_312.animateScrollToItem(index)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(70.dp)
                                            .width(250.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        ),
                                        border = BorderStroke(
                                            width = 2.dp,
                                            color = Color(0xffA6ACB5)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (mainViewModel.whichRangeMission.isNumbers) "${index + 3}" else if (mainViewModel.whichRangeMission.isAlphabets) "${(index + 3)}" else "${(index + 3)}",
                                                color = if (isDarkMode) if (index == currentIndexRange_312) Color.White else Color.Gray else if (index == currentIndexRange_312) Color.Black else Color.Gray,
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
                                state = timesState,
                                count = 5,
                                itemWidth = 70.dp,
                                visibleItemCount = 3,
                                onScrollFinish = { currentIndexTimes = it }
                            ) { index ->
                                val isFocus = index == currentIndexTimes
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
                                                timesState.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    mainViewModel.missionData(
                                        MissionDataHandler.RepeatTimes(
                                            repeat = currentIndexTimes + 1
                                        )
                                    )

                                    Card(
                                        onClick = {
                                            scope.launch {
                                                timesState.animateScrollToItem(index)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(70.dp)
                                            .width(250.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        ),
                                        border = BorderStroke(
                                            width = 2.dp,
                                            color = Color(0xffA6ACB5)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${(index + 1)}",
                                                color = if (isDarkMode) if (index == currentIndexTimes) Color.White else Color.Gray else if (index == currentIndexTimes) Color.Black else Color.Gray,
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
            }
        }
    }

}