package com.appdev.alarmapp.ui.StartingScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.theme.backColor

@Composable
fun getStarted(controller: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.4f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Wake up refreshed everyday",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "Alarmy never fails",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            )
            Text(
                text = "to wake you up",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            )
        }
        Image(
            painter = painterResource(id = R.drawable.alarmlogo),
            contentDescription = "",
            modifier = Modifier.padding(top = 70.dp)
        )

    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        CustomButton(onClick = {
            controller.navigate(Routes.Welcome.route) {
                launchSingleTop = true
                popUpTo(Routes.GetStarted.route) {
                    inclusive = true
                }
            }
        }, text ="Get started")

    }
}
