package com.appdev.alarmapp.ui.PreivewScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.whichMissionHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MissionMenu(
    controller: NavHostController,
    mainViewModel: MainViewModel,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val locPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showRationale by remember(locPermissionState) {
        mutableStateOf(false)
    }
    var onGranted by remember {
        mutableStateOf(false)
    }

    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val context = LocalContext.current
    var showToast by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = showToast) {
        if (showToast) {
            Toast.makeText(context, "Device doesn't contain required sensor !", Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
    }
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
    }
    LaunchedEffect(key1 = Unit) {
//        if (currentState !is BillingResultState.Success) {
//            mainViewModel.missionData(MissionDataHandler.ResetData)
//        }
        mainViewModel.updateSentenceList(emptySet())
    }
    BackHandler {
        if (mainViewModel.managingDefault) {
            controller.navigate(Routes.DefaultSettingsScreen.route) {
                popUpTo(Routes.SettingsOfAlarmScreen.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        } else {
            controller.navigate(Routes.Preview.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
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
                    .fillMaxWidth(0.63f)
                    .padding(vertical = 10.dp, horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    onClick = {
                        if (mainViewModel.managingDefault) {
                            controller.navigate(Routes.DefaultSettingsScreen.route) {
                                popUpTo(Routes.SettingsOfAlarmScreen.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            controller.navigate(Routes.Preview.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.size(27.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }

                Text(
                    text = "Mission",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Wake your brain",
                    fontSize = 15.sp,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 14.dp, bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    singleCard(
                        isDarkMode,
                        iconID = Icons.Filled.AutoAwesomeMosaic,
                        title = "Memory"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Memory"))
                        mainViewModel.whichMissionHandle(
                            whichMissionHandler.thisMission(
                                missionMemory = true,
                                missionMath = false,
                                missionShake = false,
                                isSteps = false, isSquat = false
                            )
                        )
                        controller.navigate(Routes.CommonMissionScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                    singleCard(
                        isDarkMode = isDarkMode,
                        iconID = Icons.Filled.Keyboard,
                        title = "Typing"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Typing"))
                        controller.navigate(Routes.TypeMissionScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                    singleCard(
                        isDarkMode = isDarkMode,
                        iconID = Icons.Filled.Calculate,
                        title = "Math"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Math"))
                        mainViewModel.whichMissionHandle(
                            whichMissionHandler.thisMission(
                                missionMemory = false,
                                missionMath = true,
                                missionShake = false,
                                isSteps = false, isSquat = false
                            )
                        )
                        controller.navigate(Routes.CommonMissionScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }

                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Wake your body",
                    fontSize = 15.sp,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 14.dp, bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    singleCard(
                        isDarkMode = isDarkMode,
                        imageId = R.drawable.shoes,
                        iconID = Icons.Filled.DirectionsWalk,
                        title = "Step"
                    ) {
                        if (isKitkatWithStepSensor(context)) {
                            if (locPermissionState.status.shouldShowRationale) {
                                showRationale = true
                            } else if (locPermissionState.status.isGranted) {
                                showToast = false
                                mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Step"))
                                mainViewModel.whichMissionHandle(
                                    whichMissionHandler.thisMission(
                                        missionMemory = false,
                                        missionMath = false,
                                        missionShake = false,
                                        isSteps = true, isSquat = false
                                    )
                                )
                                controller.navigate(Routes.CommonMissionScreen.route) {
                                    popUpTo(Routes.MissionMenuScreen.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                locPermissionState.launchPermissionRequest()
                            }
                        } else {
                            showToast = true
                        }

                    }
                    singleCard(
                        isDarkMode = isDarkMode,
                        iconID = Icons.Filled.QrCode2,
                        title = "QR/Barcode"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "QR/Barcode"))
                        controller.navigate(Routes.BarCodeDemoScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                    singleCard(
                        isDarkMode = isDarkMode,
                        iconID = Icons.Filled.ScreenRotation,
                        title = "Shake"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Shake"))
                        mainViewModel.whichMissionHandle(
                            whichMissionHandler.thisMission(
                                missionMemory = false,
                                missionMath = false,
                                missionShake = true,
                                isSteps = false, isSquat = false
                            )
                        )
                        controller.navigate(Routes.CommonMissionScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    singleCard(
                        isDarkMode = isDarkMode,
                        iconID = Icons.Filled.CameraEnhance,
                        title = "Photo"
                    ) {
                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Photo"))
                        controller.navigate(Routes.CameraRoutineScreen.route) {
                            popUpTo(Routes.MissionMenuScreen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
//                    singleCard(
//                        isDarkMode = isDarkMode,
//                        imageId = R.drawable.strength,
//                        iconID = Icons.Filled.DirectionsWalk,
//                        title = "Squat"
//                    ) {
//                        showToast = false
//                        mainViewModel.missionData(MissionDataHandler.MissionName(missionName = "Squat"))
//                        mainViewModel.whichMissionHandle(
//                            whichMissionHandler.thisMission(
//                                missionMemory = false,
//                                missionMath = false,
//                                missionShake = false,
//                                isSteps = false, isSquat = true
//                            )
//                        )
//                        controller.navigate(Routes.CommonMissionScreen.route) {
//                            popUpTo(controller.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
                }
                if (showRationale) {
                    AlertDialog(
                        onDismissRequest = {
                            showRationale = false
                        },
                        title = {
                            Text(
                                text = "Permissions required by the Application",
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                        },
                        text = {
                            Text(
                                text = "The Application requires the permissions to work",
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRationale = false
                                    locPermissionState.launchPermissionRequest()
                                },
                            ) {
                                Text("Continue", color = MaterialTheme.colorScheme.surfaceTint)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showRationale = false
                                },
                            ) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.surfaceTint)
                            }
                        },
                    )
                }


            }
        }
    }
}

fun isKitkatWithStepSensor(context: Context): Boolean {
    // Check that the device supports the step counter and detector sensors
    val packageManager = context.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun singleCard(
    isDarkMode: Boolean,
    imageId: Int = -1,
    iconID: ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .size(100.dp)
            .background(
                if (isDarkMode) Color(0xff222325) else Color(0xFFBDEAF5),
                RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            onClick = { onClick() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xff5B5AED))
        ) {
            Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                if (imageId != -1) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = iconID,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.surfaceTint,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W400,
        )
    }
}