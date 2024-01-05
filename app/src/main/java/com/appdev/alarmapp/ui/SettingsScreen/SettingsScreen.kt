package com.appdev.alarmapp.ui.SettingsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.StartingScreens.DemoScreens.buttonToPlay
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper

@Composable
fun SettingsScreen(controller: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp, vertical = 35.dp)
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 25.sp, textAlign = TextAlign.Start,
                    fontWeight = FontWeight.W500,
                )
            }
            OptionMenu(title = "Upgrade to Premium", resourceID = R.drawable.premiumquality) {
                controller.navigate(Routes.Purchase.route) {
                    popUpTo(Routes.Setting.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            OptionMenu(title = "Optimize Usability", resourceID = R.drawable.rocket) {
                controller.navigate(Routes.SetUsabilityScreen.route) {
                    popUpTo(Routes.Setting.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            OptionMenu(title = "Alarm Setting") {
                controller.navigate(Routes.SettingsOfAlarmScreen.route) {
                    popUpTo(Routes.Setting.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            Divider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 15.dp, bottom = 5.dp)
            )
            OptionMenu(title = "Dismiss Alarm/Mission") {
                controller.navigate(Routes.AlarmDismissScreen.route) {
                    popUpTo(Routes.SettingsOfAlarmScreen.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            OptionMenu(title = "General") {

            }
            OptionMenu(title = "Send feedback") {

            }
            Divider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 15.dp, bottom = 5.dp)
            )
            OptionMenu(title = "About") {

            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionMenu(
    resourceID: Int = -1,
    title: String,
    onClick: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(9.dp))
        Card(
            onClick = {
                onClick()
            },
            modifier = Modifier
                .height(65.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed ,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xff24272E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resourceID != -1) {
                    Image(
                        painter = painterResource(id = resourceID),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 3.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = "",
                        tint = Color.White.copy(
                            alpha = 0.6f
                        ),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}