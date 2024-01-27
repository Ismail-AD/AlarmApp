package com.appdev.alarmapp.ui.SettingsScreen.InnerScreens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.NotificationService
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.utils.isEmailValid
import com.appdev.alarmapp.utils.listOfOptions
import com.appdev.alarmapp.utils.listOfSensi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(mainViewModel: MainViewModel, controller: NavHostController) {

    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(true) }
    var feedbackMsg by remember { mutableStateOf(false) }
    var feedbackmail by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = showToast) {
        if (showToast) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            showToast = false
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
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        if (feedbackMsg) {
                            feedbackMsg = false
                            feedbackType = true
                        } else if (feedbackmail) {
                            feedbackmail = false
                            feedbackMsg = true
                        }
                    },
                    border = if (feedbackMsg || feedbackmail) BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceTint
                    ) else BorderStroke(0.dp, Color.Transparent),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(23.dp), contentAlignment = Alignment.Center) {
                        if (feedbackMsg || feedbackmail) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.surfaceTint
                            )
                        }
                    }
                }

                Text(
                    text = "Feedback",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(end = 35.dp)
                )
                Box(
                    modifier = Modifier
                        .size(23.dp)
                        .clickable {
                            controller.popBackStack()
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }
            if (feedbackType) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 15.dp),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Start,
                    text = "What type of feedback will you give us?",
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .background(Color.Transparent, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) elementBack else Color(0xffE5EBF7))
                            .padding(vertical = 10.dp)
                    ) {

                        listOfOptions.forEach { item ->
                            Spacer(modifier = Modifier.height(7.dp))
                            SingleAttemptSenstivity(
                                isSelected = item == selectedOption,
                                onCLick = {
                                    selectedOption = item
                                },
                                title = item
                            )
                        }
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 10.dp), contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedOption.trim().isEmpty()) {
                            Text(
                                "Please select an option",
                                fontSize = 13.sp,
                                letterSpacing = 0.sp,
                                color = Color(0xff93969F),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            )
                        }
                        CustomButton(
                            onClick = {
                                feedbackType = false
                                feedbackMsg = true
                            },
                            text = "Next",
                            isEnabled = selectedOption.trim().isNotEmpty(),
                            width = 0.95f, height = 60.dp
                        )
                    }
                }
            }
            if (feedbackMsg) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 15.dp),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Start,
                    text = "Tell us your situation or thoughts.",
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .background(Color.Transparent, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = userInput,
                            onValueChange = {
                                userInput = it
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400,
                                color = MaterialTheme.colorScheme.surfaceTint
                            ), cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                            modifier = Modifier
                                .background(
                                    if (isDarkMode) elementBack else Color(
                                        0xffE5EBF7
                                    )
                                )
                                .fillMaxWidth()
                                .height(250.dp),
                            maxLines = 8, decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.padding(
                                        start = 13.dp,
                                        end = 13.dp,
                                        bottom = 10.dp, top = 10.dp
                                    )
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 10.dp), contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (userInput.trim().isEmpty()) {
                            Text(
                                "Please enter a message",
                                fontSize = 13.sp,
                                letterSpacing = 0.sp,
                                color = Color(0xff93969F),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            )
                        }
                        CustomButton(
                            onClick = {
                                feedbackMsg = false
                                feedbackmail = true
                            },
                            text = "Next",
                            isEnabled = userInput.trim().isNotEmpty(),
                            width = 0.95f, height = 60.dp
                        )
                    }
                }
            }
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
            if (feedbackmail) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 15.dp),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Start,
                    text = "Let us know your email address to send reply to.",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    BasicTextField(
                        value = userEmail,
                        onValueChange = {
                            userEmail = it
                        },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.surfaceTint
                        ), cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if(isDarkMode) elementBack else Color(0xffE5EBF7)), singleLine = true,
                        maxLines = 1, decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(
                                    start = 13.dp,
                                    end = 13.dp,
                                    bottom = 10.dp, top = 10.dp
                                )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 10.dp), contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!isEmailValid(userEmail.trim())) {
                            Text(
                                "Please enter a valid email",
                                fontSize = 13.sp,
                                letterSpacing = 0.sp,
                                color = Color(0xff93969F),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            )
                        }
                        CustomButton(
                            onClick = {
                                loading = true
                                if (isEmailValid(userEmail.trim())) {
                                    mainViewModel.sendFeedback(
                                        "Type of feedback : $selectedOption  " +
                                                "Message: $userInput   " +
                                                "Email: $userEmail"
                                    ) { submitOrNot, Msg ->
                                        loading = false
                                        if (submitOrNot) {
                                            showToast = true
                                            errorMessage = Msg
                                            controller.popBackStack()
                                        } else {
                                            showToast = true
                                            errorMessage = Msg
                                        }
                                    }
                                }
                            },
                            text = "Send feedback",
                            isEnabled = userEmail.trim()
                                .isNotEmpty() && isEmailValid(userEmail.trim()),
                            width = 0.95f, height = 60.dp
                        )
                    }
                }
            }
        }
    }
}

