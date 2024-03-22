package com.appdev.alarmapp.navigation

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MainUI.MainUiScreen
import com.appdev.alarmapp.ui.NotificationScreen.NotificationScreen
import com.appdev.alarmapp.ui.SettingUpScreen.SettingUpScreen
import com.appdev.alarmapp.ui.StartingScreens.DemoScreens.DemoTimeScreen
import com.appdev.alarmapp.ui.StartingScreens.DemoScreens.PatternPick
import com.appdev.alarmapp.ui.StartingScreens.DemoScreens.SoundPreferScreen
import com.appdev.alarmapp.ui.StartingScreens.WelcomeScreen
import com.appdev.alarmapp.ui.StartingScreens.getStarted


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun navGraph(
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    isToken: TokenManagement,
    applicationContext: Context,
) {

    val mainViewModel: MainViewModel = hiltViewModel()
    val alarmScheduler = AlarmScheduler(applicationContext,mainViewModel)
    val start = if (isToken.getToken() != null) {
        Routes.MainUIScreen.route
    } else {
        Routes.GetStarted.route
    }
    NavHost(
        navController = controller,
        startDestination = start, enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 300),
                initialAlpha = 0.75f
            )
        }, popExitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 300),
                targetAlpha = 0.75f
            )
        }, exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 300),
                targetAlpha = 0.75f
            )
        }, popEnterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 300),
                initialAlpha = 0.75f
            )
        }, modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(route = Routes.GetStarted.route) {
            getStarted(controller)
        }
        composable(route = Routes.DemoTime.route) {
            DemoTimeScreen(controller,mainViewModel)
        }
        composable(route = Routes.Welcome.route) {
            WelcomeScreen(navController = controller)
        }
        composable(route = Routes.ToneSelection.route) {
            SoundPreferScreen(controller = controller,mainViewModel)
        }
        composable(route = Routes.Pattern.route) {
            PatternPick(controller,mainViewModel)
        }
        composable(route = Routes.Setting.route) {
            SettingUpScreen(controller)
        }
        composable(route = Routes.Notify.route) {
            NotificationScreen(controller)
        }
        composable(route = Routes.MainUIScreen.route) {
            MainUiScreen(alarmScheduler,textToSpeech,isToken, mainViewModel = mainViewModel)
        }
    }
}