package com.appdev.alarmapp.ui.SettingsScreen

import android.widget.ScrollView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.appdev.alarmapp.ui.MainScreen.MainViewModel

@Composable
fun SettingsScreen(controller: NavHostController, mainViewModel: MainViewModel) {
    BackHandler {
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 20.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 20.dp)
                )
            }
            item {
                OptionMenu(
                    title = "Upgrade to Premium",
                    resourceID = R.drawable.premiumquality
                ) {
                    controller.navigate(Routes.Purchase.route) {
                        popUpTo(Routes.Setting.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                OptionMenu(
                    title = "Optimize Usability",
                    resourceID = R.drawable.rocket
                ) {
                    controller.navigate(Routes.SetUsabilityScreen.route) {
                        popUpTo(Routes.Setting.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                OptionMenu(title = "Alarm Setting") {
                    controller.navigate(Routes.SettingsOfAlarmScreen.route) {
                        popUpTo(Routes.Setting.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                Divider(
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 10.dp)
                )
            }
            item {
                OptionMenu(title = "Dismiss Alarm/Mission") {
                    controller.navigate(Routes.AlarmDismissScreen.route) {
                        popUpTo(Routes.SettingsOfAlarmScreen.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                OptionMenu(title = "General") {
                    controller.navigate(Routes.GeneralScreen.route) {
                        popUpTo(Routes.SettingsOfAlarmScreen.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                OptionMenu(title = "Send feedback") {
                    controller.navigate(Routes.FeedbackScreen.route) {
                        popUpTo(Routes.SettingsOfAlarmScreen.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
            item {
                Divider(
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 10.dp)
                )
            }
            item {
                OptionMenu(title = "About") {

                }
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
                containerColor = MaterialTheme.colorScheme.inverseOnSurface
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
                    color = MaterialTheme.colorScheme.surfaceTint,
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
                        tint = MaterialTheme.colorScheme.surfaceTint.copy(
                            alpha = 0.6f
                        ),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}