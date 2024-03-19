package com.appdev.alarmapp.ui.PreivewScreen

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.gentleWakeup
import com.appdev.alarmapp.utils.newAlarmHandler
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(InternalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SoundPowerUp(
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }
    var loading by remember { mutableStateOf(true) }
    var playing by remember { mutableStateOf(false) }
    var playing2 by remember { mutableStateOf(false) }
    var playing3 by remember { mutableStateOf(false) }
    var startItNow by remember { mutableStateOf(false) }
    var wakeUpSwitch by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.isGentleWakeUp else mainViewModel.newAlarm.isGentleWakeUp) }
    var timeReminderSwitch by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.isTimeReminder else mainViewModel.newAlarm.isTimeReminder) }
    var loudEffectSwitch by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.isLoudEffect else mainViewModel.newAlarm.isLoudEffect) }
    var showWakeSheet by remember { mutableStateOf(false) }
    var selectedOptionLimit by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.wakeUpTime.toString() else mainViewModel.newAlarm.wakeUpTime.toString()) }
    val context = LocalContext.current
    val sheetStatePower = rememberModalBottomSheetState()
    var showSecondsSheet by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
        loading = false
    }
    DisposableEffect(textToSpeech) {
        onDispose {
            textToSpeech.stop()
        }
    }

    LaunchedEffect(key1 = startItNow) {
        if (startItNow) {
            Helper.playStream(context, R.raw.alarmsound)
        } else {
            Helper.stopStream()
        }
    }
    val scope = rememberCoroutineScope()
    BackHandler {
        if (mainViewModel.whichAlarm.isOld) {
            mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = selectedOptionLimit.toInt()))
            mainViewModel.updateHandler(EventHandlerAlarm.TimeReminder(isTimeReminderOrNot = timeReminderSwitch))
            mainViewModel.updateHandler(EventHandlerAlarm.IsGentleWakeUp(isGentleWakeUp = wakeUpSwitch))
            mainViewModel.updateHandler(EventHandlerAlarm.LoudEffect(isLoudEffectOrNot = loudEffectSwitch))
        } else if (!mainViewModel.whichAlarm.isOld) {
            mainViewModel.newAlarmHandler(newAlarmHandler.GetWakeUpTime(getWUTime = selectedOptionLimit.toInt()))
            mainViewModel.newAlarmHandler(newAlarmHandler.TimeReminder(isTimeReminderOrNot = timeReminderSwitch))
            mainViewModel.newAlarmHandler(newAlarmHandler.IsGentleWakeUp(isGentleWakeUp = wakeUpSwitch))
            mainViewModel.newAlarmHandler(newAlarmHandler.LoudEffect(isLoudEffectOrNot = loudEffectSwitch))
        }
        controller.navigate(Routes.Preview.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }
    Scaffold(topBar = {
        TopBar(
            width = 0.87f,
            isDarkMode = isDarkMode,
            title = "Sound power-up",
            actionText = "",
            backColor = MaterialTheme.colorScheme.background
        ) {
            if (mainViewModel.whichAlarm.isOld) {
                mainViewModel.updateHandler(EventHandlerAlarm.GetWakeUpTime(getWUTime = selectedOptionLimit.toInt()))
                mainViewModel.updateHandler(EventHandlerAlarm.TimeReminder(isTimeReminderOrNot = timeReminderSwitch))
                mainViewModel.updateHandler(EventHandlerAlarm.IsGentleWakeUp(isGentleWakeUp = wakeUpSwitch))
                mainViewModel.updateHandler(EventHandlerAlarm.LoudEffect(isLoudEffectOrNot = loudEffectSwitch))
            } else if (!mainViewModel.whichAlarm.isOld) {
                mainViewModel.newAlarmHandler(newAlarmHandler.GetWakeUpTime(getWUTime = selectedOptionLimit.toInt()))
                mainViewModel.newAlarmHandler(newAlarmHandler.TimeReminder(isTimeReminderOrNot = timeReminderSwitch))
                mainViewModel.newAlarmHandler(newAlarmHandler.IsGentleWakeUp(isGentleWakeUp = wakeUpSwitch))
                mainViewModel.newAlarmHandler(newAlarmHandler.LoudEffect(isLoudEffectOrNot = loudEffectSwitch))
            }
            controller.navigate(Routes.Preview.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Dialog(onDismissRequest = { /*TODO*/ }) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 11.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp))
            ) {
                SingleFeature(isLock = false,title = "Gentle wake-up", isDarkMode, wakeUpSwitch) {
                    wakeUpSwitch = it
                }
                if (wakeUpSwitch) {
                    SingleOption(
                        title = "Volume increase\ntime",
                        data = if (selectedOptionLimit.toInt() > 10) "$selectedOptionLimit seconds" else "$selectedOptionLimit minutes",
                        isDarkMode
                    ) {
                        showSecondsSheet = true
                    }
                }
            }
            featureTest(
                title = "Starts from minimum volume and\nrises to the volume you set",
                playing = playing
            ) {
                playing = !playing
                if (playing) {
                    Helper.updateLow(true)
                    Helper.startIncreasingVolume()
                    Helper.playStream(context, R.raw.alarmsound)
                } else {
                    Helper.stopStream()
                    Helper.updateLow(false)
                    Helper.stopIncreasingVolume()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 11.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp))
            ) {
                SingleFeature(currentState !is BillingResultState.Success,title = "Time Reminder", isDarkMode, timeReminderSwitch) {
//                    if(currentState is BillingResultState.Success) {
                        timeReminderSwitch = it
//                    } else{
//                        controller.navigate(Routes.Purchase.route) {
//                            popUpTo(Routes.SoundPowerUpScreen.route){
//                                inclusive=false
//                            }
//                            launchSingleTop = true
//                        }
//                    }
                }

            }
            featureTest(
                title = "Read out current time every time while your alarm is ringing",
                playing = playing2
            ) {
                playing2 = !playing2
                if (playing2) {
                    playCurrentTimeAndDate(
                        textToSpeech,
                        System.currentTimeMillis().toString()
                    ) { endOrNot ->
                        startItNow = endOrNot
                    }
                } else {
                    startItNow = false
                    textToSpeech.stop()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 11.dp)
                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp))
            ) {
                SingleFeature(currentState !is BillingResultState.Success,title = "Extra Loud Effect", isDarkMode, loudEffectSwitch) {
//                    if(currentState is BillingResultState.Success){
                        loudEffectSwitch = it
//                    } else{
//                        controller.navigate(Routes.Purchase.route) {
//                            popUpTo(Routes.SoundPowerUpScreen.route){
//                                inclusive=false
//                            }
//                            launchSingleTop = true
//                        }
//                    }

                }

            }
            featureTest(
                title = "Rings a super loud alarm if you are unresponsive for 40 seconds",
                playing = playing3
            ) {
                playing3 = !playing3
                if (playing3) {
                    Helper.playStream(context, R.raw.alarmsound)
                    scope.launch {
                        delay(5000L)
                        setMaxVolume(context)
                        Helper.playStream(context, R.raw.loudeffect)
                    }
                } else {
                    Helper.stopStream()
                }
            }

        }
        if (showSecondsSheet) {
                ModalBottomSheet(onDismissRequest = {
                    showSecondsSheet = false
                 }, sheetState = sheetStatePower){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(vertical = 16.dp, horizontal = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gentle wake-up",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            gentleWakeup.forEach { item ->
                                Spacer(modifier = Modifier.height(7.dp))
                                WakeUpLimit(
                                    isSelected = item == selectedOptionLimit,
                                    onCLick = {
                                        selectedOptionLimit = item
                                        showWakeSheet = false
                                        mainViewModel.updateDismissSettings(
                                            DismissSettings(
                                                id = mainViewModel.dismissSettings.value.id,
                                                dismissTime = mainViewModel.dismissSettings.value.dismissTime,
                                                missionTime = selectedOptionLimit.toInt(),
                                                photoSensitivity = mainViewModel.dismissSettings.value.photoSensitivity,
                                                muteTone = mainViewModel.dismissSettings.value.muteTone
                                            )
                                        )
                                    },
                                    title = item
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
        }
    }
}


@Composable
fun SingleFeature(
    isLock: Boolean = false,
    title: String,
    isDarkMode: Boolean,
    switchState: Boolean,
    update: (Boolean) -> Unit
) {
    var switchState by remember { mutableStateOf(switchState) }
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.surfaceTint,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 5.dp)
            )
            if(isLock){
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Switch(
            checked = switchState,
            onCheckedChange = { newSwitchState ->
//                if(isLock){
//                    update(false)
//                } else{
                    switchState = newSwitchState
                    update(switchState)
//                }
                // Handle the new switch state
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
            ), modifier = Modifier.scale(0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun featureTest(
    title: String,
    playing: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.inverseSurface,
            textAlign = TextAlign.Start, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(0.7f)
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
                    color = MaterialTheme.colorScheme.surfaceTint
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
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Sample", fontSize = 13.sp,
                        letterSpacing = 0.sp,
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }

        }
    }
}

fun playCurrentTimeAndDate(textToSpeech: TextToSpeech, id: String, startItNow: (Boolean) -> Unit) {
    textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
        }

        override fun onDone(utteranceId: String?) {
            startItNow(true)
        }

        override fun onError(utteranceId: String?) {
        }
    })

    val params = Bundle()
    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDateAndTime: String = sdf.format(Date())

    // Extracting only hour, minute, date, and day
    val formattedTimeAndDate = SimpleDateFormat("hh:mm a EEEE, MMMM d, yyyy", Locale.getDefault())
        .format(sdf.parse(currentDateAndTime) ?: Date())

    // Play the formatted time and date as audio
    textToSpeech.speak(formattedTimeAndDate, TextToSpeech.QUEUE_FLUSH, params, id)
}

fun setMaxVolume(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
}

@Composable
fun WakeUpLimit(isSelected: Boolean, onCLick: () -> Unit, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onCLick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xff18677E),
                unselectedColor = Color(0xffB6BDCA)
            )
        )
        Text(
            text = if (title.toInt() > 10) "$title seconds" else "$title minutes",
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 17.sp, modifier = Modifier.padding(start = 6.dp)
        )
    }
}