package com.appdev.alarmapp.ui.NotificationScreen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.ui.theme.backColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationScreen(controller: NavHostController) {

    val notifyPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        TODO("VERSION.SDK_INT < TIRAMISU")
    }
    var showRationale by remember(notifyPermissionState) {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    LaunchedEffect(notifyPermissionState.status) {
        if (notifyPermissionState.status.isGranted) {
            controller.navigate(Routes.MainUIScreen.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.alarmlogo),
                contentDescription = ""
            )
            Text(
                text = "Important Point for Alarmy!",
                color = Color(0xffC33A48),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xff1C1F26), shape = RoundedCornerShape(5.dp))
                ) {
                    Text(
                        text = "Allow Notification",
                        color = Color.White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Permission",
                        color = Color.White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(), fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "if notifications are turned off,you",
                        color = Color.White,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "wont see or hear anything when",
                        color = Color.White,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = "alarm rings",
                        color = Color.White,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            CustomButton(
                                onClick = { /*TODO*/ },
                                text = "Don't Allow",
                                width = 0.40f,
                                backgroundColor = Color(0xff3F434F)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            CustomButton(
                                onClick = { /*TODO*/ },
                                text = "Allow",
                                width = 0.75f
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.leftclick),
                            contentDescription = "",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CustomButton(onClick = {
                if (notifyPermissionState.status.shouldShowRationale) {
                    showRationale = true
                } else {
                    notifyPermissionState.launchPermissionRequest()
                }
            }, text = "Ok, I got it.")

        }
    }
    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
            },
            title = {
                Text(text = "Permissions required by the Application")
            },
            text = {
                Text(text = "The Application requires the following permissions to work:\n POST_NOTIFICATIONS")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        openAppSettings(context)
                    },
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                    },
                ) {
                    Text("Dismiss")
                }
            },
        )
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}

//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        NotificationScreen(controller = rememberNavController())
//    }
//}