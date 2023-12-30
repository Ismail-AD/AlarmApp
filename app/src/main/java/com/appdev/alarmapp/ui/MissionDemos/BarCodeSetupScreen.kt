package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.CustomImageButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.openAppSettings
import com.appdev.alarmapp.ui.theme.backColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun BarCodeMissionDemo(controller: NavHostController, mainViewModel: MainViewModel) {

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current

    var showRationale by remember(permissionState) {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    var bottomSheetState by remember {
        mutableStateOf(false)
    }
    var guideOrNot by remember {
        mutableStateOf(permissionState.status.isGranted)
    }
    var showToast by remember(permissionState) {
        mutableStateOf(false)
    }
    var showEmptyToast by remember {
        mutableStateOf(false)
    }
    var filename by remember {
        mutableStateOf(mainViewModel.uiState.value.detectedQR)
    }
    val uiState by mainViewModel.uiState.collectAsState()


    LaunchedEffect(uiState.detectedQR) {
        if(uiState.detectedQR.trim().isNotEmpty()){
            bottomSheetState = true
            filename = uiState.detectedQR
        }
    }

    LaunchedEffect(showEmptyToast) {
        if (showEmptyToast) {
            Toast.makeText(context, "File name should not be Empty !", Toast.LENGTH_SHORT).show()
            showEmptyToast = false
        }
    }

//    val imagesList by mainViewModel.imagesList.collectAsStateWithLifecycle(
//        initialValue = emptyList()
//    )
    var loading by remember { mutableStateOf(false) }
    var selectedCodeIndex by remember { mutableLongStateOf(-1) }
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            guideOrNot = permissionState.status.isGranted
        }
    }
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "Photo not selected! Please select a photo", Toast.LENGTH_SHORT)
                .show()
            showToast = false
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
//                        if (selectedImageIndex > 1) {
//                            if (!mainViewModel.isRealAlarm) {
//                                Helper.playStream(context, R.raw.alarmsound)
//                            }
//                            controller.navigate(Routes.PreviewAlarm.route) {
//                                popUpTo(controller.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
//                        } else {
//                            showToast = true
//                        }
                    },
                    text = "Preview",
                    width = 0.3f,
                    backgroundColor = backColor,
                    isBorderPreview = true,
                    textColor = Color.LightGray
                )
                Spacer(modifier = Modifier.width(14.dp))
                CustomButton(
                    onClick = {
//                        if (selectedImageIndex > 1) {
//                            mainViewModel.missionData(
//                                MissionDataHandler.IsSelectedMission(
//                                    isSelected = true
//                                )
//                            )
//                            mainViewModel.missionData(
//                                MissionDataHandler.ImageId(mainViewModel.selectedImage.id)
//                            )
//                            mainViewModel.missionData(MissionDataHandler.SubmitData)
//                            controller.navigate(Routes.MissionMenuScreen.route) {
//                                popUpTo(controller.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
//                        } else {
//                            showToast = true
//                        }

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
                    .background(backColor)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.71f)
                        .padding(vertical = 10.dp, horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        onClick = {
                            controller.navigate(Routes.MissionMenuScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        border = BorderStroke(1.dp, Color.White),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(modifier = Modifier.size(27.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "QR/Barcode",
                        color = Color.White,
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
                                .background(Color(0xff2F333E), shape = RoundedCornerShape(10.dp)),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(
                                text = "Scan a code of a part of your morning routine",
                                color = Color.White,
                                fontSize = 21.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Image(
                                painter = painterResource(id = R.drawable.qr),
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
                            text = "Scan",
                            width = 0.8f,
                            backgroundColor = Color.White,
                            textColor = Color.Black.copy(alpha = 0.9f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Card(
                                    onClick = {
                                        controller.navigate(Routes.BarCodeScanScreen.route) {
                                            popUpTo(controller.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }, shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xff3F434F
                                        )
                                    ), modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp)
                                        .padding(horizontal = 5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "",
                                            tint = Color(0xffA6ACB5),
                                            modifier = Modifier.size(25.dp)
                                        )
                                        Text(
                                            text = "Add",
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
//                            items(imagesList) { imageData ->
//                                singleEntry(imageData = imageData,
//                                    isSelected = selectedImageIndex == imageData.id,
//                                    onImageClick = {
//                                        selectedImageIndex = imageData.id
//                                        mainViewModel.updateSelectedImage(imageData)
//                                    }) {
//                                    mainViewModel.deleteImage(imageData.id)
//                                }
//                            }
                        }
                    }
                }
                if (bottomSheetState) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            if (filename.isEmpty()) {
//                                audioFile?.let {
//                                    mainViewModel.insertRecording(
//                                        Ringtone(
//                                            file = it,
//                                            name = getDefaultFilename()
//                                        )
//                                    )
//                                }
                                showEmptyToast = false
                                bottomSheetState = false
                            } else {
//                                audioFile?.let {
//                                    mainViewModel.insertRecording(
//                                        Ringtone(
//                                            file = it,
//                                            name = getDefaultFilename()
//                                        )
//                                    )
//                                }
                                showEmptyToast = false
                                bottomSheetState = false
                            }
                        },
                        sheetState = sheetState,
                        dragHandle = {}) {
                        Column(
                            modifier = Modifier.background(Color(0xff1C1F26)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "QR/Barcode name",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.73f),
                                    textAlign = TextAlign.End, fontWeight = FontWeight.W500
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    IconButton(onClick = {
                                        if (filename.isEmpty()) {
                                            showEmptyToast = true
                                        } else {
                                            // Save to database
                                            showEmptyToast = false
                                            bottomSheetState = false
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)) {
                                BasicTextField(
                                    value = filename,
                                    onValueChange = {
                                        filename = it
                                    }, cursorBrush = SolidColor(Color.White), singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f), textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                    maxLines = 1, decorationBox = { innerTextField ->
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
                                Divider(thickness = 1.dp, color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.8f))
                            }


                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp), contentAlignment = Alignment.Center
                            ) {
                                CustomButton(onClick = {
                                    if (filename.isEmpty()) {
                                        showEmptyToast = true
                                    } else {
//                                        audioFile?.let {
//                                            mainViewModel.insertRecording(
//                                                Ringtone(
//                                                    file = it,
//                                                    name = getDefaultFilename()
//                                                )
//                                            )
//                                        }
                                        showEmptyToast = false
                                        bottomSheetState = false
                                    }

                                }, text = "Save", width = 0.8f)
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


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun singleEntry(
//    imageData: ImageData,
//    isSelected: Boolean,
//    onImageClick: () -> Unit,
//    deleteImage: () -> Unit
//) {
//    Card(
//        onClick = {
//            onImageClick()
//        },
//        modifier = Modifier
//            .padding(5.dp)
//            .width(110.dp)
//            .height(130.dp),
//        shape = RoundedCornerShape(10.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xff3F434F))
//    ) {
//        Box(
//            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
//        ) {
//            imageData.bitmap?.let {
//                Image(
//                    bitmap = it.asImageBitmap(),
//                    contentDescription = "",
//                    contentScale = ContentScale.Crop
//                )
//            }
//            if (!isSelected) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(top = 2.dp, end = 2.dp), contentAlignment = Alignment.TopEnd
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(22.dp)
//                            .background(Color(0xff222325), CircleShape)
//                            .clickable {
//                                deleteImage()
//                            }, contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Tick",
//                            tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(17.dp)
//                        )
//                    }
//                }
//            }
//            if (isSelected) {
//                Box(
//                    modifier = Modifier
//                        .background(Color.Black.copy(alpha = 0.45f))
//                        .fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // Your tick icon here
//                    Icon(
//                        imageVector = Icons.Default.Check,
//                        contentDescription = "Tick",
//                        tint = Color(0xff13a8c4), modifier = Modifier.size(70.dp)
//                    )
//                }
//            }
//        }
//    }
//}