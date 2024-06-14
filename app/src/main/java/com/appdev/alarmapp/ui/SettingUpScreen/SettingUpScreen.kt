package com.appdev.alarmapp.ui.SettingUpScreen

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.ui.theme.backColor
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun SettingUpScreen(controller: NavHostController) {
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sharedanim))
    LaunchedEffect(animatedProgress) {
        var elapsedTime = 0L
        val duration = 2000L
        while (elapsedTime < duration) {
            val deltaTime = min(10, duration - elapsedTime)
            elapsedTime += deltaTime
            delay(deltaTime)
            progress += deltaTime.toFloat() / duration
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = {
                    animatedProgress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = Color.White,
                trackColor = backColor,
            )
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                )
                Text(
                    text = "Setting up your alarm…",
                    color = Color.White,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    fontWeight = FontWeight.Bold
                )
                if (progress >= 1.0f) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        controller.navigate(Routes.Notify.route) {
                            launchSingleTop = true
                            popUpTo(Routes.Setting.route) {
                                inclusive = true
                            }
                        }
                        // Navigate to the Notify screen for Android versions 12 and below

                    } else {
                        // Navigate to a different screen for Android versions above 12
                        // Modify the destination route accordingly
                        controller.navigate(Routes.MainUIScreen.route) {
                            launchSingleTop = true
                            popUpTo(Routes.Setting.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }
}


//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        SettingUpScreen()
//    }
//}

