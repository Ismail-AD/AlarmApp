package com.appdev.alarmapp.ui.SleepScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ui.theme.backColor

@Composable
fun SleepScreen(controller: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(backColor), contentAlignment = Alignment.Center){
        Text(text = "SLEEP", color = Color.White)
    }
}