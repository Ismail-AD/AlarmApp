package com.appdev.alarmapp.ui.Analysis

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.newAlarmHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LabelScreen(
    textToSpeech: TextToSpeech,
    controller: NavHostController,
    mainViewModel: MainViewModel
) {
    var filename by remember {
        mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.labelTextForSpeech else mainViewModel.newAlarm.labelTextForSpeech)
    }
    var switchState by remember { mutableStateOf(if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.isLabel else mainViewModel.newAlarm.isLabel) }
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    var startItNow by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var playing by remember { mutableStateOf(false) }

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = filename,
                selection = TextRange(filename.length)
            )
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        keyboardController?.show()
        focusRequester.requestFocus()
    }


    DisposableEffect(textToSpeech) {
        onDispose {
            textToSpeech.stop()
        }
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "Cannot be used without adding a label !", Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
    }

    LaunchedEffect(key1 = textFieldValueState.text) {
        if (playing) {
            textToSpeech.stop()
            Helper.stopStream()
            playing = false
        }
        if (textFieldValueState.text.trim().isEmpty()) {
            switchState = false
        }
    }

    LaunchedEffect(key1 = startItNow) {
        if (startItNow) {
            Helper.playStream(context, R.raw.alarmsound)
        } else {
            Helper.stopStream()
        }
    }
    BackHandler {
        if (mainViewModel.whichAlarm.isOld) {
            mainViewModel.updateHandler(
                EventHandlerAlarm.IsLabel(
                    isLabelOrNot = switchState
                )
            )
            mainViewModel.updateHandler(
                EventHandlerAlarm.LabelText(
                    getLabelText = textFieldValueState.text
                )
            )
        } else {
            mainViewModel.newAlarmHandler(
                newAlarmHandler.IsLabel(
                    isLabelOrNot = switchState
                )
            )
            mainViewModel.newAlarmHandler(
                newAlarmHandler.LabelText(
                    getLabelText = textFieldValueState.text
                )
            )
        }
        controller.navigate(Routes.Preview.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }


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
                    .fillMaxWidth(0.56f)
                    .padding(vertical = 10.dp, horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        if (mainViewModel.whichAlarm.isOld) {
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.IsLabel(
                                    isLabelOrNot = switchState
                                )
                            )
                            mainViewModel.updateHandler(
                                EventHandlerAlarm.LabelText(
                                    getLabelText = textFieldValueState.text
                                )
                            )
                        } else {
                            mainViewModel.newAlarmHandler(
                                newAlarmHandler.IsLabel(
                                    isLabelOrNot = switchState
                                )
                            )
                            mainViewModel.newAlarmHandler(
                                newAlarmHandler.LabelText(
                                    getLabelText = textFieldValueState.text
                                )
                            )
                        }
                        controller.navigate(Routes.Preview.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
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
                    text = "Label",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)) {
                    BasicTextField(
                        value = textFieldValueState,
                        onValueChange = {
                            textFieldValueState = it
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(
                                    start = 3.dp,
                                    end = 2.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(horizontal = 8.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp, horizontal = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Reads out the label when your alarm rings",
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.Start,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Card(
                                    onClick = {
                                        playing = !playing
                                        if (playing) {
                                            playText(
                                                textToSpeech, text = textFieldValueState.text,
                                                id = System.currentTimeMillis().toString()
                                            ) { endOrNot ->
                                                startItNow = endOrNot
                                            }
                                        } else {
                                            startItNow = false
                                            textToSpeech.stop()
                                        }
                                    },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(85.dp),
                                    shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (textFieldValueState.text.trim()
                                                .isEmpty()
                                        ) Color(0x434C5460) else MaterialTheme.colorScheme.surfaceTint
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (textFieldValueState.text.trim()
                                                .isEmpty()
                                        ) Color(
                                            0x434C5460
                                        ) else Color.Transparent,
                                        disabledContainerColor = if (textFieldValueState.text.trim()
                                                .isEmpty()
                                        ) Color(
                                            0x434C5460
                                        ) else Color.Transparent
                                    ), enabled = textFieldValueState.text.trim()
                                        .isNotEmpty()
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
                                            "Preview", fontSize = 13.sp,
                                            letterSpacing = 0.sp,
                                            color = MaterialTheme.colorScheme.surfaceTint
                                        )
                                    }
                                }

                            }
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Label Reminder",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 16.sp, modifier = Modifier.fillMaxWidth(0.85f)
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Switch(
                                    checked = switchState,
                                    onCheckedChange = { newSwitchState ->
                                        if (textFieldValueState.text.trim().isEmpty()) {
                                            showToast = true
                                        } else {
                                            switchState = newSwitchState
                                            // Handle the new switch state
                                        }
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
}

fun playText(textToSpeech: TextToSpeech, id: String, text: String, startItNow: (Boolean) -> Unit) {
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

    // Play the formatted time and date as audio
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
}