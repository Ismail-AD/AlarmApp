package com.appdev.alarmapp.ui.MainUI

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.AlarmManagement.DismissCallback
import com.appdev.alarmapp.AlarmManagement.SnoozeCallback
import com.appdev.alarmapp.AlarmManagement.TimerEndsCallback
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.AlarmCancelScreen
import com.appdev.alarmapp.ui.Analysis.LabelScreen
import com.appdev.alarmapp.ui.MainScreen.MainScreen
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionDemos.BarCodeMissionDemo
import com.appdev.alarmapp.ui.MissionDemos.CameraMissionDemo
import com.appdev.alarmapp.ui.MissionDemos.LocationMissionSetup
import com.appdev.alarmapp.ui.MissionDemos.MemoryMissionScreen
import com.appdev.alarmapp.ui.MissionDemos.PhotoClickScreen
import com.appdev.alarmapp.ui.MissionDemos.RangeMemoryMissionSetting
import com.appdev.alarmapp.ui.MissionDemos.ScanBarCodeScreen
import com.appdev.alarmapp.ui.MissionDemos.SentenceSelection
import com.appdev.alarmapp.ui.MissionDemos.TypingMissionScreen
import com.appdev.alarmapp.ui.MissionDemos.showMap
import com.appdev.alarmapp.ui.MissionViewer.ArrangeAlphabetsMHScreen
import com.appdev.alarmapp.ui.MissionViewer.ArrangeNumbersMHScreen
import com.appdev.alarmapp.ui.MissionViewer.ArrangeShapesMHScreen
import com.appdev.alarmapp.ui.MissionViewer.AtLocationMission
import com.appdev.alarmapp.ui.MissionViewer.BarCodeMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.MathMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.MissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.PhotoMissionScreen
import com.appdev.alarmapp.ui.MissionViewer.RangedAlphabetsMissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.RangedNumbersMissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.ShakeDetectionScreen
import com.appdev.alarmapp.ui.MissionViewer.SquatMission
import com.appdev.alarmapp.ui.MissionViewer.StepMission
import com.appdev.alarmapp.ui.MissionViewer.TypingMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.WalkOffMission
import com.appdev.alarmapp.ui.PreivewScreen.MissionMenu
import com.appdev.alarmapp.ui.PreivewScreen.PreviewScreen
import com.appdev.alarmapp.ui.PreivewScreen.RingtoneSelection
import com.appdev.alarmapp.ui.PreivewScreen.SoundPowerUp
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.AlarmDismissSettings
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.AlarmSettings
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.DefaultAlarmMissions
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.FeedbackScreen
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.GeneralSettings
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.SetTheme
import com.appdev.alarmapp.ui.SettingsScreen.SettingsScreen
import com.appdev.alarmapp.ui.SettingsScreen.InnerScreens.UsabilityScreen
import com.appdev.alarmapp.ui.Snooze.SnoozeScreen
import com.appdev.alarmapp.ui.inappbuyScreen.InAppPurchase
import com.appdev.alarmapp.utils.BottomNavItems
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.isOldOrNew
import com.appdev.alarmapp.utils.newAlarmHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun MainUiScreen(
    ringtoneRepository: RingtoneRepository,
    textToSpeech: TextToSpeech,
    tokenManagement: TokenManagement,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    if (tokenManagement.getToken() == null) {
        tokenManagement.saveToken(System.currentTimeMillis().toString())
        mainViewModel.insertDefaultSettings(
            DefaultSettings()
        )
        mainViewModel.basicSettingsInsertion(
            AlarmSetting()
        )
        mainViewModel.dismissSettingsInsertion(
            DismissSettings()
        )
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        scope.launch(Dispatchers.IO) {
            mainViewModel.deleteAllRingtones()
            val rings = getSystemRingtones(context)
            mainViewModel.insertSystemList(rings)
        }
    }
    val isDarkMode by mainViewModel.themeSettings.collectAsState()

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
        Routes.CameraRoutineScreen.route,
        Routes.PhotoClickScreen.route,
        Routes.PhotoMissionPreviewScreen.route,
        Routes.BarCodeDemoScreen.route,
        Routes.BarCodeScanScreen.route,
        Routes.BarCodePreviewAlarmScreen.route,
        Routes.SetUsabilityScreen.route,
        Routes.SettingsOfAlarmScreen.route,
        Routes.DefaultSettingsScreen.route,
        Routes.AlarmDismissScreen.route,
        Routes.FeedbackScreen.route,
        Routes.GeneralScreen.route,
        Routes.ThemeChangeScreen.route,
        Routes.SoundPowerUpScreen.route,
        Routes.LabelScreen.route,
        Routes.SquatMissionScreen.route,
        Routes.SnoozeScreen.route,
        Routes.RangeMemoryMissionSetting.route,
        Routes.RangeMemoryMissionPreview.route,
        Routes.RangeAlphabetMissionPreview.route,
        Routes.ReachLocationMissionScreen.route,
        Routes.MapScreen.route,
        Routes.WalkOffScreen.route,
        Routes.AtLocationMissionScreen.route,
        Routes.ArrangeNumbersScreen.route,
        Routes.ArrangeAlphabetsScreen.route,
        Routes.ArrangeShapesScreen.route,
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
                        mainViewModel.newAlarmHandler(newAlarmHandler.GetWakeUpTime(getWUTime = 30))
                        mainViewModel.newAlarmHandler(
                            newAlarmHandler.TimeReminder(
                                isTimeReminderOrNot = false
                            )
                        )
                        mainViewModel.newAlarmHandler(newAlarmHandler.IsGentleWakeUp(isGentleWakeUp = true))
                        mainViewModel.newAlarmHandler(newAlarmHandler.CustomVolume(customVolume = 100f))
                        mainViewModel.newAlarmHandler(newAlarmHandler.LoudEffect(isLoudEffectOrNot = false))
                        mainViewModel.newAlarmHandler(newAlarmHandler.skipAlarm(skipAlarm = false))
                        mainViewModel.newAlarmHandler(newAlarmHandler.IsLabel(isLabelOrNot = false))
                        mainViewModel.newAlarmHandler(newAlarmHandler.LabelText(getLabelText = ""))
                        mainViewModel.newAlarmHandler(newAlarmHandler.getTime(time = LocalTime.now()))
                        mainViewModel.newAlarmHandler(newAlarmHandler.getSnoozeTime(getSnoozeTime = mainViewModel.defaultSettings.value.snoozeTime))
                        mainViewModel.newAlarmHandler(
                            newAlarmHandler.ringtone(
                                ringtone = mainViewModel.defaultSettings.value.ringtone
                            )
                        )
                        mainViewModel.newAlarmHandler(
                            newAlarmHandler.getMissions(missions = mainViewModel.defaultSettings.value.listOfMissions)
                        )
                        mainViewModel.missionData(MissionDataHandler.AddList(missionsList = mainViewModel.defaultSettings.value.listOfMissions))
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
                    backgroundColor = if (isDarkMode) Color(0xff2C2C2C) else Color.White,
                    cutoutShape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 30.dp)
                ) {
                    Spacer(modifier = Modifier.weight(0.15f))
                    BottomNavItems.Home.let { home ->
                        NavItem(isDarkMode, item = home, isSelected = home.id == selectedIndex) {
                            selectedIndex = home.id
                            controller.navigate(Routes.MainScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    BottomNavItems.Settings.let { set ->
                        NavItem(isDarkMode, item = set, isSelected = set.id == selectedIndex) {
                            selectedIndex = set.id
                            controller.navigate(Routes.FifthScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.15f))
                }
            }
        }, modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) { PV ->
        NavHost(
            navController = controller,
            startDestination = Routes.MainScreen.route, modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable(route = Routes.MainScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                MainScreen(ringtoneRepository, controller, mainViewModel)
            }

            composable(route = Routes.SquatMissionScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SquatMission(
                    mainViewModel = mainViewModel,
                    controller = controller,
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }
            composable(route = Routes.LabelScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                LabelScreen(textToSpeech, controller, mainViewModel)
            }

            composable(route = Routes.TypingPreviewScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                TypingMissionHandler(textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }
                    },
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }

            composable(route = Routes.FeedbackScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                FeedbackScreen(mainViewModel = mainViewModel, controller = controller)
            }

            composable(route = Routes.FifthScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SettingsScreen(controller, mainViewModel)
            }
            composable(route = Routes.SoundPowerUpScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SoundPowerUp(textToSpeech, controller, mainViewModel)
            }
            composable(route = Routes.SetUsabilityScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                UsabilityScreen(controller)
            }
            composable(route = Routes.AlarmDismissScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                AlarmDismissSettings(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.GeneralScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                GeneralSettings(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.ThemeChangeScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SetTheme(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.SettingsOfAlarmScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                AlarmSettings(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.DefaultSettingsScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                DefaultAlarmMissions(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.Purchase.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                InAppPurchase(controller)
            }
            composable(route = Routes.Preview.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                PreviewScreen(
                    ringtoneRepository,
                    textToSpeech,
                    controller,
                    mainViewModel
                )
            }
            composable(route = Routes.Ringtone.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                RingtoneSelection(
                    controller,
                    mainViewModel
                )
            }
            composable(route = Routes.PreviewAlarm.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                AlarmCancelScreen(
                    textToSpeech = textToSpeech,
                    onDismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    },
                    snoozeCallback = object : SnoozeCallback {
                        override fun onSnoozeClicked() {
                            TODO("Not yet implemented")
                        }

                    },
                    controller = controller,
                    mainViewModel = mainViewModel,
                    ringtoneRepository = ringtoneRepository
                )
            }
            composable(route = Routes.MissionShakeScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                ShakeDetectionScreen(textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller, timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }
            composable(route = Routes.CameraRoutineScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                CameraMissionDemo(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.PhotoMissionPreviewScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                PhotoMissionScreen(textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    },
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }
            composable(route = Routes.PhotoClickScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                PhotoClickScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.BarCodeDemoScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                BarCodeMissionDemo(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.BarCodeScanScreen.route, enterTransition = {
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
                }) {
                ScanBarCodeScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.BarCodePreviewAlarmScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                BarCodeMissionScreen(textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    },
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }
            composable(route = Routes.StepDetectorScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                StepMission(textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller, timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    })
            }
            composable(route = Routes.SnoozeScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SnoozeScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.CommonMissionScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                MemoryMissionScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.RangeMemoryMissionSetting.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                RangeMemoryMissionSetting(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.TypeMissionScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                TypingMissionScreen(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.SentenceScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                SentenceSelection(mainViewModel = mainViewModel, controller = controller)
            }
            composable(route = Routes.MissionMenuScreen.route, enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(600)
                )
            },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(600)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(600)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(600)
                    )
                }) {
                MissionMenu(controller = controller, mainViewModel)
            }
            composable(route = Routes.MapScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                showMap(mainViewModel = mainViewModel, controller)
            }

            composable(route = Routes.MissionMathScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                MathMissionHandler(textToSpeech = textToSpeech,
                    mainViewModel = mainViewModel,
                    missionLevel = mainViewModel.missionDetails.missionLevel,
                    controller = controller, timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(route = Routes.ReachLocationMissionScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                LocationMissionSetup(controller = controller, mainViewModel)
            }

            composable(route = Routes.AtLocationMissionScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                AtLocationMission(
                    textToSpeech = textToSpeech, mainViewModel = mainViewModel,
                    controller = controller,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    },
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(route = Routes.WalkOffScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                WalkOffMission(
                    textToSpeech = textToSpeech,
                    mainViewModel = mainViewModel,
                    controller = controller,
                    timerEndsCallback = object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    },
                    dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(Routes.RangeAlphabetMissionPreview.route, enterTransition = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(
                        220, delayMillis = 90
                    )
                )
            },
                exitTransition = {
                    fadeOut(animationSpec = tween(90))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(90))
                }) {
                val diffLevel = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> mainViewModel.missionDetails.valuesToPick * 2
                    "Hard Mode" -> mainViewModel.missionDetails.valuesToPick * 3
                    else -> 0
                }

                val cubeHeightWidth =
                    when (mainViewModel.missionDetails.difficultyLevel) {
                        "Normal Mode" -> 60.dp
                        "Hard Mode" -> 50.dp
                        else -> 60.dp
                    }
                val columnPadding = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 4.dp
                    "Hard Mode" -> 3.dp
                    else -> 1.dp
                }
                val lazyRowHeight = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 65.dp
                    "Hard Mode" -> 55.dp
                    else -> 65.dp
                }
                RangedAlphabetsMissionHandlerScreen(
                    textToSpeech = textToSpeech,
                    cubeHeightWidth = cubeHeightWidth,
                    colPadding = columnPadding,
                    rowHeight = lazyRowHeight,
                    controller = controller,
                    totalSize = diffLevel,
                    mainViewModel = mainViewModel,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(Routes.ArrangeShapesScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                ArrangeShapesMHScreen(
                    textToSpeech = textToSpeech,
                    controller = controller,
                    mainViewModel = mainViewModel,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(Routes.ArrangeAlphabetsScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                ArrangeAlphabetsMHScreen(
                    textToSpeech = textToSpeech,
                    controller = controller,
                    mainViewModel = mainViewModel,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(Routes.ArrangeNumbersScreen.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                ArrangeNumbersMHScreen(
                    textToSpeech = textToSpeech,
                    controller = controller,
                    mainViewModel = mainViewModel,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }
            composable(Routes.RangeMemoryMissionPreview.route, enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            exitTransition = {
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
            },
            popExitTransition = {
                    fadeOut(animationSpec = tween(90))
            }) {
                val diffLevel = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> mainViewModel.missionDetails.valuesToPick * 2
                    "Hard Mode" -> mainViewModel.missionDetails.valuesToPick * 3
                    else -> 0
                }

                val cubeHeightWidth =
                    when (mainViewModel.missionDetails.difficultyLevel) {
                        "Normal Mode" -> 60.dp
                        "Hard Mode" -> 50.dp
                        else -> 60.dp
                    }
                val columnPadding = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 4.dp
                    "Hard Mode" -> 3.dp
                    else -> 1.dp
                }
                val lazyRowHeight = when (mainViewModel.missionDetails.difficultyLevel) {
                    "Normal Mode" -> 65.dp
                    "Hard Mode" -> 55.dp
                    else -> 65.dp
                }
                RangedNumbersMissionHandlerScreen(
                    textToSpeech = textToSpeech,
                    cubeHeightWidth = cubeHeightWidth,
                    colPadding = columnPadding,
                    rowHeight = lazyRowHeight,
                    controller = controller,
                    totalSize = diffLevel,
                    mainViewModel = mainViewModel,
                    timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
                )
            }


            composable(route = Routes.MissionScreen.route, enterTransition = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(
                        220, delayMillis = 90
                    )
                )
            },
                exitTransition = {
                    fadeOut(animationSpec = tween(90))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            220, delayMillis = 90
                        )
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(90))
                }) {
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
                    textToSpeech = textToSpeech,
                    cubeHeightWidth = cubeHeightWidth, colPadding = columnPadding,
                    rowHeight = lazyRowHeight,
                    controller = controller, totalSize = sizeOfBlocks,
                    mainViewModel = mainViewModel, timerEndsCallback =
                    object : TimerEndsCallback {
                        override fun onTimeEnds() {
                            TODO("Not yet implemented")
                        }

                    }, dismissCallback = object : DismissCallback {
                        override fun onDismissClicked() {
                            // Provide implementation here if needed
                        }
                    }
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
fun NavItem(isDarkMode: Boolean, item: BottomNavItems, isSelected: Boolean, onCLick: () -> Unit) {
    Image(
        painter = painterResource(id = if (isSelected && (isDarkMode)) item.selectedId else if (isSelected) item.darkSelectedId else item.unSelectedId),
        modifier = Modifier
            .size(24.dp)
            .clickable { onCLick() },
        contentDescription = "",
    )
}

fun getSystemRingtones(context: Context): List<Ringtone> {
    val ringtoneManager = RingtoneManager(context)
    ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

    val cursor = ringtoneManager.cursor
    val mergedRingtoneList = mutableListOf<Ringtone>()

    while (cursor.moveToNext()) {
        val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
        val ringtoneName = getRingtoneTitle(context, ringtoneUri)
        val mergedRingtone = Ringtone(ringtoneName, uri = ringtoneUri)
        mergedRingtoneList.add(mergedRingtone)
    }

    cursor.close()
    return mergedRingtoneList
}


fun getRingtoneTitle(context: Context, ringtoneUri: Uri): String {
    val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
    return ringtone.getTitle(context) ?: "Unknown Ringtone"
}