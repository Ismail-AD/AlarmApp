package com.appdev.alarmapp.ui.MissionDemos

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor


@OptIn(ExperimentalGetImage::class)
@Composable
fun ScanBarCodeScreen(controller: NavHostController, mainViewModel: MainViewModel) {

    var isFlashOn by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        controller.navigate(Routes.BarCodeDemoScreen.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
    ) {

        if (isFlashOn) {
            BarCodeCameraPreview(viewModel = mainViewModel) {
                controller.navigate(Routes.BarCodeDemoScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        } else {
            BarCodeCameraPreview(viewModel = mainViewModel) {
                controller.navigate(Routes.BarCodeDemoScreen.route) {
                    popUpTo(controller.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    isFlashOn = !isFlashOn
                    mainViewModel.updateFlash(isFlashOn)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                    tint = Color.Gray
                )
            }
        }
    }
}