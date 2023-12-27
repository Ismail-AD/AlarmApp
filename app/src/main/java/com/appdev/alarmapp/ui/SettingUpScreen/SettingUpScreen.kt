package com.appdev.alarmapp.ui.SettingUpScreen

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
    var currentStep by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value
    LaunchedEffect(animatedProgress) {
        var elapsedTime = 0L
        val duration = 2000L // 3 seconds
        while (elapsedTime < duration) {
            val deltaTime = min(10, duration - elapsedTime)
            elapsedTime += deltaTime
            delay(deltaTime)
            progress += deltaTime.toFloat() / duration

            // Check if the progress has reached a threshold to move to the next step
            if (progress >= 0.35f && currentStep == 0) {
                currentStep = 1
            } else if (progress >= 0.66f && currentStep == 1) {
                currentStep = 2
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff24272E)), contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Be the next one",
                    color = Color(0xffEFD08E),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 25.dp, vertical = 10.dp)
                        .background(Color(0xff2F333E), shape = RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .height(100.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "4 , 5 1 2 , 4 7 8",
                        color = Color.White,
                        fontSize = 35.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.29f)
                .background(backColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    trackColor = backColor,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), progress = animatedProgress
                )
                Column(modifier = Modifier.padding(start = 20.dp, top = 25.dp)) {
                    Text(
                        text = "All set!",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Call the belowAllSet functions based on the current step
                    if (currentStep >= 0) {
                        belowAllSet(text = "Setting up alarm time")
                    }
                    if (currentStep >= 1) {
                        belowAllSet(text = "Setting up brain activating sound")
                    }
                    if (currentStep >= 2) {
                        belowAllSet(text = "Setting up wake-up mission")
                    }
                    if (progress >= 1.0f) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Log.d("VECHK","BELOW 12")
                            controller.navigate(Routes.Notify.route) {
                                launchSingleTop = true
                                popUpTo(Routes.Setting.route) {
                                    inclusive = true
                                }
                            }
                            // Navigate to the Notify screen for Android versions 12 and below

                        } else {
                            Log.d("VECHK","NOT BELOW 12")
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
}

@Composable
fun belowAllSet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, top = 12.dp)
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animfile))
        val progress by animateLottieCompositionAsState(composition)
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp, modifier = Modifier.padding(start = 13.dp)
        )
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