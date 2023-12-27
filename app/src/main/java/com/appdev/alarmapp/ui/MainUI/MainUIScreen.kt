package com.appdev.alarmapp.ui.MainUI

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.AlarmCancelScreen
import com.appdev.alarmapp.ui.Analysis.AnalysisScreen
import com.appdev.alarmapp.ui.MainScreen.MainScreen
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionDemos.MemoryMissionScreen
import com.appdev.alarmapp.ui.MissionDemos.SentenceSelection
import com.appdev.alarmapp.ui.MissionDemos.TypingMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.MathMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.MissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.ShakeDetectionScreen
import com.appdev.alarmapp.ui.MissionViewer.StepMission
import com.appdev.alarmapp.ui.MissionViewer.TypingMissionHandler
import com.appdev.alarmapp.ui.PreivewScreen.MissionMenu
import com.appdev.alarmapp.ui.PreivewScreen.PreviewScreen
import com.appdev.alarmapp.ui.PreivewScreen.RingtoneSelection
import com.appdev.alarmapp.ui.SettingsScreen.SettingsScreen
import com.appdev.alarmapp.ui.SleepScreen.SleepScreen
import com.appdev.alarmapp.ui.Snooze.SnoozeScreen
import com.appdev.alarmapp.ui.SunScreen.MorningScreen
import com.appdev.alarmapp.ui.inappbuyScreen.InAppPurchase
import com.appdev.alarmapp.utils.BottomNavItems
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.isOldOrNew
import com.appdev.alarmapp.utils.newAlarmHandler
import java.time.LocalTime
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainUiScreen(
    tokenManagement: TokenManagement,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel, inAppPurchaseClick: () -> Unit
) {
    if (tokenManagement.getToken() == null) {
        tokenManagement.saveToken(System.currentTimeMillis().toString())
    }
    var selectedIndex by remember { mutableIntStateOf(0) }
//    val navItems = remember { BottomNavItems.}
    val backStackEntry = controller.currentBackStackEntryAsState()
    val screenWithNoBar = listOf(
        Routes.Purchase.route,
        Routes.Preview.route,
        Routes.Ringtone.route,
        Routes.PreviewAlarm.route,
        Routes.MissionScreen.route,
        Routes.MissionMathScreen.route,
        Routes.MissionShakeScreen.route,
        Routes.SnoozeScreen.route,
        Routes.CommonMissionScreen.route,
        Routes.TypeMissionScreen.route,
        Routes.MissionMenuScreen.route,
        Routes.SentenceScreen.route,
        Routes.TypingPreviewScreen.route,
        Routes.StepDetectorScreen.route,
    )
    androidx.compose.material.Scaffold(
        floatingActionButtonPosition = androidx.compose.material.FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            if (backStackEntry.value?.destination?.route !in screenWithNoBar) {
                androidx.compose.material.FloatingActionButton(
                    onClick = {
                        mainViewModel.isOld(isOldOrNew.isOld(false))
                        mainViewModel.missionData(MissionDataHandler.ResetList)
                        mainViewModel.newAlarmHandler(newAlarmHandler.getTime(time = LocalTime.now()))
                        mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = 5))
                        mainViewModel.newAlarmHandler(
                            newAlarmHandler.ringtone(
                                ringtone = Ringtone(
                                    name = "Alarm Bell",
                                    rawResourceId = R.raw.alarmsound
                                )
                            )
                        )
                        mainViewModel.newAlarmHandler(newAlarmHandler.getDays(days = emptySet()))
                        controller.navigate(Routes.Preview.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    backgroundColor = Color(0xff7B70FF),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "BTN",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (backStackEntry.value?.destination?.route !in screenWithNoBar) {
                BottomAppBar(
                    backgroundColor = Color(0xff1C1F26),
                    cutoutShape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 30.dp)
                ) {
                    BottomNavItems.Home.let { home ->
                        NavItem(item = home, isSelected = home.id == selectedIndex) {
                            selectedIndex = home.id
                            controller.navigate(Routes.MainScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    BottomNavItems.Sleep.let { Sleep ->
                        NavItem(item = Sleep, isSelected = Sleep.id == selectedIndex) {
                            selectedIndex = Sleep.id
                            controller.navigate(Routes.SecondScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    BottomNavItems.Morning.let { home ->
                        NavItem(item = home, isSelected = home.id == selectedIndex) {
                            selectedIndex = home.id
                            controller.navigate(Routes.ThirdScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    BottomNavItems.Settings.let { set ->
                        NavItem(item = set, isSelected = set.id == selectedIndex) {
                            selectedIndex = set.id
                            controller.navigate(Routes.FifthScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }) { PV ->
        NavHost(
            navController = controller,
            startDestination = Routes.MainScreen.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                ExitTransition.None
            }
        ) {
            composable(route = Routes.MainScreen.route) {
                MainScreen(controller, mainViewModel)
            }
            composable(route = Routes.SecondScreen.route) {
                SleepScreen(controller)
            }
            composable(route = Routes.ThirdScreen.route) {
                MorningScreen(controller)
            }
            composable(route = Routes.FourthScreen.route) {
                AnalysisScreen(controller)
            }

            composable(route = Routes.TypingPreviewScreen.route) {
                TypingMissionHandler(mainViewModel = mainViewModel, controller = controller)
            }

            composable(route = Routes.FifthScreen.route) {
                SettingsScreen(controller)
            }
            composable(route = Routes.Purchase.route) {
                InAppPurchase(controller, onCLick = { inAppPurchaseClick() })
            }
            composable(route = Routes.Preview.route) {
                PreviewScreen(
                    controller,
                    mainViewModel
                )
            }
            composable(route = Routes.Ringtone.route) {
                RingtoneSelection(
                    controller,
                    mainViewModel
                )
            }
            composable(route = Routes.PreviewAlarm.route) {
                AlarmCancelScreen(controller, mainViewModel)
            }
            composable(route = Routes.MissionShakeScreen.route) {
                ShakeDetectionScreen(mainViewModel = mainViewModel, controller)
            }
            composable(route = Routes.StepDetectorScreen.route) {
                StepMission(mainViewModel = mainViewModel, controller)
            }
            composable(route = Routes.SnoozeScreen.route) {
                SnoozeScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.CommonMissionScreen.route) {
                MemoryMissionScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.TypeMissionScreen.route) {
                TypingMissionScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.SentenceScreen.route) {
                SentenceSelection(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.MissionMenuScreen.route) {
                MissionMenu(controller = controller,mainViewModel)
            }
            composable(route = Routes.MissionMathScreen.route) {
                MathMissionHandler(
                    mainViewModel,
                    missionLevel = mainViewModel.missionDetails.missionLevel,
                    controller = controller
                )
            }
            composable(route = Routes.MissionScreen.route) {
                val sizeOfBlocks = when (mainViewModel.missionDetails.missionLevel) {
                    "Very Easy" -> 3
                    "Easy" -> 4
                    "Normal" -> 5
                    "Hard" -> 6
                    else -> 3
                }
                val cubeHeightWidth =
                    when (mainViewModel.missionDetails.missionLevel) {
                        "Very Easy" -> 100.dp
                        "Easy" -> 75.dp
                        "Normal" -> 61.dp
                        "Hard" -> 50.dp
                        else -> 100.dp
                    }
                val columnPadding = when (mainViewModel.missionDetails.missionLevel) {
                    "Very Easy" -> 8.dp
                    "Easy" -> 4.dp
                    "Normal" -> 1.dp
                    "Hard" -> 0.dp
                    else -> 8.dp
                }
                val lazyRowHeight = when (mainViewModel.missionDetails.missionLevel) {
                    "Very Easy" -> 100.dp
                    "Easy" -> 80.dp
                    "Normal" -> 65.dp
                    "Hard" -> 55.dp
                    else -> 100.dp
                }
                MissionHandlerScreen(
                    cubeHeightWidth, columnPadding, lazyRowHeight,
                    controller, totalSize = sizeOfBlocks,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}


fun Modifier.NoRippleClickable(onCLick: () -> Unit): Modifier = composed {
    clickable(indication = null, interactionSource = remember {
        MutableInteractionSource()
    }) {
        onCLick()
    }
}

@Composable
fun NavItem(item: BottomNavItems, isSelected: Boolean, onCLick: () -> Unit) {
    Image(
        painter = painterResource(id = if (isSelected) item.selectedId else item.unSelectedId),
        modifier = Modifier
            .size(24.dp)
            .clickable { onCLick() },
        contentDescription = "",
    )
}

