package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService


@Composable
fun GeneralSettings(mainViewModel: MainViewModel, controller: NavHostController) {
    val switchState by mainViewModel.themeSettings.collectAsState()
    val isDarkMode by mainViewModel.themeSettings.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 10.dp, horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        controller.popBackStack()
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(23.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }

                Text(
                    text = "General",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Column {
                Spacer(modifier = Modifier.height(9.dp))
                Card(
                    onClick = {

                    },
                    modifier = Modifier
                        .height(68.dp)
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed ,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "theme",
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 3.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Switch(
                                checked = switchState,
                                onCheckedChange = { newSwitchState ->
                                    mainViewModel.updateThemeSettings(newSwitchState)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isDarkMode) Color.White else Color(
                                        0xff13A7CB
                                    ), // Color when switch is ON
                                    checkedTrackColor = if (isDarkMode) Color(0xff7358F5) else Color(
                                        0xff7FCFE1
                                    ), // Track color when switch is ON
                                    uncheckedThumbColor = if (isDarkMode) Color(0xff949495) else Color(
                                        0xff656D7D
                                    ), // Color when switch is OFF
                                    uncheckedTrackColor = if (isDarkMode) Color(0xff343435) else Color(
                                        0xff9E9E9E
                                    ) // Track color when switch is OFF
                                ), modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
