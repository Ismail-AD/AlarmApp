package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.openAppSettings
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.MissionDataHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraMissionDemo(controller: NavHostController, mainViewModel: MainViewModel, checkOutViewModel: checkOutViewModel = hiltViewModel()) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }



    var showRationale by remember(permissionState) {
        mutableStateOf(false)
    }
    var guideOrNot by remember {
        mutableStateOf(permissionState.status.isGranted)
    }
    var showToast by remember(permissionState) {
        mutableStateOf(false)
    }
    val imagesList by mainViewModel.imagesList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var loading by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableLongStateOf(if (mainViewModel.selectedImage.id > 1) mainViewModel.selectedImage.id else if (mainViewModel.missionDetails.imageId > 1) mainViewModel.missionDetails.imageId else -1) }
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            guideOrNot = permissionState.status.isGranted
        }
    }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
    }
    LaunchedEffect(key1 = imagesList) {
        loading = imagesList.isEmpty()
        delay(700)
        if (imagesList.isEmpty()) {
            loading = false
        }
    }
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "Photo not selected! Please select a photo", Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
    }
    val backStackEntry = controller.currentBackStackEntryAsState()

    BackHandler {
        controller.navigate(Routes.MissionMenuScreen.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    Scaffold(bottomBar = {
        if (guideOrNot) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .fillMaxWidth()
            ) {
                CustomButton(
                    onClick = {
                        if (selectedImageIndex > 1) {
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(Routes.CameraRoutineScreen.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            showToast = true
                        }
                    },
                    text = "Preview",
                    width = 0.3f,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    isBorderPreview = true,
                    textColor = if (isDarkMode) Color.LightGray else Color.Black
                )
                Spacer(modifier = Modifier.width(14.dp))
                CustomButton(
                    onClick = {
                        if (mainViewModel.managingDefault) {
                            if (selectedImageIndex > 1) {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.ImageId(mainViewModel.selectedImage.id)
                                )
                                mainViewModel.missionData(MissionDataHandler.SubmitData)
                                mainViewModel.setDefaultSettings(
                                    DefaultSettingsHandler.GetNewObject(
                                        defaultSettings = DefaultSettings(
                                            id = mainViewModel.defaultSettings.value.id,
                                            ringtone = mainViewModel.defaultSettings.value.ringtone,
                                            snoozeTime = mainViewModel.defaultSettings.value.snoozeTime,
                                            listOfMissions = mainViewModel.missionDetailsList
                                        )
                                    )
                                )
                                mainViewModel.setDefaultSettings(DefaultSettingsHandler.UpdateDefault)
                                controller.navigate(Routes.DefaultSettingsScreen.route) {
                                    popUpTo(Routes.SettingsOfAlarmScreen.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                showToast = true
                            }
                        } else {
//                            if (currentState !is BillingResultState.Success) {
//                                mainViewModel.missionData(MissionDataHandler.ResetList)
//                            }
                            if (selectedImageIndex > 1) {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.ImageId(mainViewModel.selectedImage.id)
                                )
                                mainViewModel.missionData(MissionDataHandler.SubmitData)
                                controller.navigate(Routes.Preview.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            } else {
                                showToast = true
                            }
                        }

                    },
                    text = "Complete",
                    width = 0.83f,
                    backgroundColor = Color(0xff7B70FF),
                    textColor = Color.White
                )
            }
        }
    }) { it ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it), contentAlignment = Alignment.TopCenter
        ) {
//        if (loading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Dialog(onDismissRequest = { /*TODO*/ }) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//        }

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .padding(vertical = 10.dp, horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        onClick = {
                            controller.popBackStack()
//                            controller.navigate(Routes.MissionMenuScreen.route) {
//                                popUpTo(controller.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
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
                        text = "Photo",
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                    )
                }
                if (!guideOrNot) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 22.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(
                                text = "Take a photo of part of your morning routine",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 21.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Image(
                                painter = painterResource(id = R.drawable.photodemo),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(300.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter
                    ) {
                        CustomButton(
                            onClick = {
                                if (permissionState.status.shouldShowRationale) {
                                    showRationale = true
                                } else {
                                    permissionState.launchPermissionRequest()
                                }
                            },
                            text = "Take a Photo",
                            width = 0.85f,
                            backgroundColor = signatureBlue,
                            textColor = Color.White
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Card(
                                    onClick = {
                                        controller.navigate(Routes.PhotoClickScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .width(110.dp)
                                        .height(130.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkMode) Color(
                                            0xff3F434F
                                        ) else Color.LightGray
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.surfaceTint,
                                                modifier = Modifier.size(25.dp)
                                            )
                                            Text(
                                                text = "Add",
                                                color = MaterialTheme.colorScheme.surfaceTint,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }

                            }
                            items(imagesList) { imageData ->
                                singleEntry(imageData = imageData,
                                    isSelected = selectedImageIndex == imageData.id,
                                    onImageClick = {
                                        selectedImageIndex = imageData.id
                                        mainViewModel.updateSelectedImage(imageData)
                                    }) {
                                    mainViewModel.deleteImage(imageData.id)
                                }
                            }
                        }
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
                            Text(text = "The Application requires the following permissions to work:\n CAMERA_ACCESS")
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
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun singleEntry(
    imageData: ImageData,
    isSelected: Boolean,
    onImageClick: () -> Unit,
    deleteImage: () -> Unit
) {
    Card(
        onClick = {
            onImageClick()
        },
        modifier = Modifier
            .padding(5.dp)
            .width(110.dp)
            .height(130.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff3F434F))
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            imageData.bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
            }
            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 2.dp, end = 2.dp), contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(0xff222325), CircleShape)
                            .clickable {
                                deleteImage()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tick",
                            tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f))
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Your tick icon here
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tick",
                        tint = Color(0xff13a8c4), modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
    }
}