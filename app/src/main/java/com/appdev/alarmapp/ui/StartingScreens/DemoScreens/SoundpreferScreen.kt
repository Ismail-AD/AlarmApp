package com.appdev.alarmapp.ui.StartingScreens.DemoScreens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.newAlarmHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPreferScreen(controller: NavHostController, mainViewModel: MainViewModel) {

    var selectedCard by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }
    var selectedAudio by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current


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
                .fillMaxSize(0.15f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose the sound you prefer",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold
            )
        }

        Column {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = {
                    selectedCard = 0
                    selectedAudio = R.raw.alarmsound
                },
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = if (selectedCard == 0) 2.dp else 0.dp,
                    color = if (selectedCard == 0) Color(0xff0FAACB) else Color.Transparent
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == 0) Color(0xff2F333E) else Color(0xff24272E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bellicon),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Alarm Bell",
                        color = Color.White,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 3.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (selectedCard == 0 && selectedAudio != -1) {
                            buttonToPlay(playing) {
                                playing = !playing
                                if (playing) {
                                    Helper.playStream(context, selectedAudio)
                                } else {
                                    Helper.stopStream()
                                }
                            }
                        }
                    }
                }
            }
        }
        Column {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = {
                    selectedCard = 1
                    selectedAudio = R.raw.peacefulsound
                },
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = if (selectedCard == 1) 2.dp else 0.dp,
                    color = if (selectedCard == 1) Color(0xff0FAACB) else Color.Transparent
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == 1) Color(0xff2F333E) else Color(0xff24272E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.smileicon),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Peaceful Sound",
                        color = Color.White,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 3.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (selectedCard == 1 && selectedAudio != -1) {
                            buttonToPlay(playing) {
                                playing = !playing
                                if (playing) {
                                    Helper.playStream(context, selectedAudio)
                                } else {
                                    Helper.stopStream()
                                }
                            }
                        }
                    }
                }
            }
        }
        Column {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = {
                    selectedCard = 2
                    selectedAudio = R.raw.cheerfulsound
                },
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = if (selectedCard == 2) 2.dp else 0.dp,
                    color = if (selectedCard == 2) Color(0xff0FAACB) else Color.Transparent
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == 2) Color(0xff2F333E) else Color(0xff24272E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.happyface),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cheerful Sound",
                        color = Color.White,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 3.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (selectedCard == 2 && selectedAudio != -1) {
                            buttonToPlay(playing) {
                                playing = !playing
                                if (playing) {
                                    Helper.playStream(context, selectedAudio)
                                } else {
                                    Helper.stopStream()
                                }
                            }
                        }
                    }
                }
            }
        }
        Column {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = {
                    selectedCard = 3
                    selectedAudio = R.raw.loudsound
                },
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = if (selectedCard == 3) 2.dp else 0.dp,
                    color = if (selectedCard == 3) Color(0xff0FAACB) else Color.Transparent
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == 3) Color(0xff2F333E) else Color(0xff24272E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mind),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Loud Sound",
                        color = Color.White,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 3.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (selectedCard == 3 && selectedAudio != -1) {
                            buttonToPlay(playing) {
                                playing = !playing
                                if (playing) {
                                    Helper.playStream(context, selectedAudio)
                                } else {
                                    Helper.stopStream()
                                }
                            }
                        }
                    }
                }
            }
        }
        Column {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = {
                    selectedCard = 4
                },
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                border = BorderStroke(
                    width = if (selectedCard == 4) 2.dp else 0.dp,
                    color = if (selectedCard == 4) Color(0xff0FAACB) else Color.Transparent
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == 4) Color(0xff2F333E) else Color(0xff24272E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nossound),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Vibration Only",
                        color = Color.White,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                }
            }
        }
        if (selectedCard != -1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomButton(onClick = {
                    val audioName = when (selectedCard) {
                        0 -> "Alarm Bell"
                        3 -> "Loud Sound"
                        2 -> "Cheerful Sound"
                        1 -> "Peaceful Sound"
                        4 -> "Silent"
                        else -> {
                            "Alarm Bell"
                        }
                    }
                    val audioPlayed = when (selectedCard) {
                        0 -> R.raw.alarmsound
                        3 -> R.raw.loudsound
                        2 -> R.raw.cheerfulsound
                        1 -> R.raw.peacefulsound
                        4 -> R.raw.silenceplease
                        else -> {
                            R.raw.alarmsound
                        }
                    }
                    mainViewModel.newAlarmHandler(
                        newAlarmHandler.ringtone(
                            Ringtone(
                                name = audioName,
                                rawResourceId = audioPlayed
                            )
                        )
                    )
                    Log.d("CHKIT",mainViewModel.newAlarm.ringtone.rawResourceId.toString())
                    controller.navigate(Routes.Pattern.route) {
                        launchSingleTop = true
                        popUpTo(Routes.ToneSelection.route) {
                            inclusive = true
                        }
                    }
                }, text = "Next")
            }
        }
    }

    DisposableEffect(key1 = selectedCard) {
        // Pause the audio if it was playing in the previous selection
        if (playing) {
            playing = false
            Helper.stopStream()
        }
        onDispose {
            playing = false
            Helper.stopStream()
        }

    }
    DisposableEffect(key1 = selectedAudio) {
        onDispose {
            playing = false
            Helper.stopStream()
            Helper.releasePlayer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun buttonToPlay(playing: Boolean, onClick: () -> Unit) {
    Card(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .size(35.dp), shape = CircleShape, colors = CardDefaults.cardColors(
            containerColor = Color(0xff17A5CB)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "",
                tint = Color.White
            )
        }
    }
}
