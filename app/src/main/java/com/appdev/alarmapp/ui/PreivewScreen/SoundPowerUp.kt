package com.appdev.alarmapp.ui.PreivewScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.utils.Helper

@Composable
fun SoundPowerUp(controller: NavHostController) {
    var playing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(topBar = {
        TopBar(title = "Sound power-up", actionText = "", backColor = Color.Transparent) {
            controller.navigate(Routes.MainScreen.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true

            }
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(backColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp)
                    .background(elementBack)
            ) {
                SingleFeature(title = "Gentle wake-up")
                SingleOption(title = "Volume increase\ntime", data = "30 secconds") {

                }
            }
            featureTest(
                title = "Starts from minimum volume and\nrises to the volume you set",
                playing = playing
            ) {
                playing = !playing
                if (playing) {
                    Helper.playStream(context, R.raw.alarmsound)
                } else {
                    Helper.stopStream()
                }

            }


        }
    }
}

@Composable
fun SingleFeature(title: String) {
    var switchState by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            textAlign = TextAlign.Start, fontSize = 16.sp
        )

        Switch(
            checked = switchState,
            onCheckedChange = { newSwitchState ->
                switchState = newSwitchState
                // Handle the new switch state
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xff12A8CB), // Color when switch is ON
                checkedTrackColor = Color(0xff18677E), // Track color when switch is ON
                uncheckedThumbColor = Color(0xffB5BCCB), // Color when switch is OFF
                uncheckedTrackColor = Color(0xff111217) // Track color when switch is OFF
            ), modifier = Modifier.scale(0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun featureTest(title: String, playing: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color(0xffA6ACB5),
            textAlign = TextAlign.Start, fontSize = 13.sp
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Card(
                onClick = {
                    onClick()
                },
                modifier = Modifier
                    .height(40.dp)
                    .width(85.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Sample", fontSize = 13.sp,
                        letterSpacing = 0.sp,
                        color = Color.White
                    )
                }
            }

        }
    }
}