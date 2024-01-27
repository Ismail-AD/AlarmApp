package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ui.theme.backColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsabilityScreen(controller: NavHostController) {
    val verticalScroll = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(verticalScroll)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.74f)
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
                    text = "Optimize Usability",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Text(
                text = "Checklist for better alarm usage environment",
                color = MaterialTheme.colorScheme.surfaceTint,
                fontSize = 19.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.W400,
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp),
                lineHeight = 29.sp
            )
            Text(
                "Please check your settings before using alarms.",
                fontSize = 14.sp,
                letterSpacing = 0.sp,
                color = Color(0xffA6ACB5),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 20.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            TipComponent(
                title = "1. Exclude From Battery Optimization",
                listOfTips = listOf(
                    "Exclude Alarmy app from battery optimization target.",
                    " Close all unused apps before going to bed. "
                )
            )
            Divider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 15.dp, bottom = 5.dp)
            )
            TipComponent(
                title = "2. Check Do Not Disturb Mode",
                listOfTips = listOf(
                    "If do not disturb mode is tuned on, allow alarm &media sound.",
                )
            )
            Divider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 15.dp, bottom = 5.dp)
            )
            TipComponent(
                title = "3. Prevent Alarm Not Ringing Due to Changes in Device Status",
                listOfTips = listOf(
                    "If Alarmy app or OS is updated, make sure to open Alarmy at least once.",
                    "When rebooting the device, V' open Alarmy after reboot as well"
                )
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun TipComponent(title: String, listOfTips: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 19.sp,
            fontWeight = FontWeight.W400, lineHeight = 29.sp
        )
        listOfTips.forEachIndexed { index, s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "",
                    tint = Color(0xffeebf37)
                )
                Text(
                    s,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            if (title == "1. Exclude From Battery Optimization" && index == 0) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                    }
                    .padding(horizontal = 13.dp)) {
                    Text(
                        "View Detail",
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffA6ACB5),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 20.dp)
                    )
                    Divider(
                        thickness = 1.dp,
                        color = Color(0xffA6ACB5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 33.dp, end = 178.dp, top = 0.dp)
                    )
                }
            } else if (title == "2. Check Do Not Disturb Mode" && index == 0) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                    }
                    .padding(horizontal = 13.dp),) {
                    Text(
                        "View Detail",
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffA6ACB5),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 20.dp)
                    )
                    Divider(
                        thickness = 1.dp,
                        color = Color(0xffA6ACB5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 33.dp, end = 178.dp, top = 0.dp)
                    )
                }
            }
        }

    }
}