package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.openAppSettings
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.QrCodeData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun BarCodeMissionDemo(controller: NavHostController, mainViewModel: MainViewModel) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    if (Helper.isPlaying()) {
        Helper.stopStream()
    }
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current

    var showRationale by remember(permissionState) {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    val configSheetState = rememberModalBottomSheetState()
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
    var clickedElement by remember {
        mutableLongStateOf(if (mainViewModel.missionDetails.codeId > 1) mainViewModel.missionDetails.codeId else if (mainViewModel.selectedCode.codeId > 1) mainViewModel.selectedCode.codeId else -1)
    }
    var updateAttempt by remember {
        mutableStateOf(false)
    }
    var configBottomBar by remember {
        mutableStateOf(false)
    }
    var filename by remember {
        mutableStateOf(mainViewModel.detectedQrCodeState.qrCode)
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(mainViewModel.detectedQrCodeState) {
        if (mainViewModel.detectedQrCodeState.qrCode.trim().isNotEmpty()) {
            bottomSheetState = true
            filename = mainViewModel.detectedQrCodeState.qrCode
        }
    }
    LaunchedEffect(key1 = bottomSheetState) {
        if (!bottomSheetState && mainViewModel.detectedQrCodeState.qrCode.trim().isNotEmpty()) {
            mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = ""))
        }
    }
    LaunchedEffect(key1 = Unit) {
        if (!mainViewModel.detectedQrCodeState.startProcess) {
            mainViewModel.updateDetectedString(
                MainViewModel.ProcessingState(
                    qrCode = "",
                    startProcess = true
                )
            )
        }
    }
    LaunchedEffect(showEmptyToast) {
        if (showEmptyToast) {
            Toast.makeText(context, "File name should not be Empty !", Toast.LENGTH_SHORT).show()
            showEmptyToast = false
        }
    }
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(
                context,
                "Code not selected! Please scan and select a code",
                Toast.LENGTH_SHORT
            )
                .show()
            showToast = false
        }
    }

    val codesList by mainViewModel.codesList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    var loading by remember { mutableStateOf(false) }
    var selectedCodeIndex by remember { mutableLongStateOf(if (mainViewModel.selectedCode.codeId > 1) mainViewModel.selectedCode.codeId else if (mainViewModel.missionDetails.codeId > 1) mainViewModel.missionDetails.codeId else -1) }
    LaunchedEffect(key1 = codesList) {
        loading = codesList.isEmpty()
        delay(700)
        if (codesList.isEmpty()) {
            loading = false
        }
    }
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            guideOrNot = permissionState.status.isGranted
        }
    }
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
                        if (selectedCodeIndex > 1) {
                            if (!mainViewModel.isRealAlarm) {
                                Helper.playStream(context, R.raw.alarmsound)
                            }
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(controller.graph.startDestinationId)
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
                            if (selectedCodeIndex > 1) {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.SelectedQrCode(mainViewModel.selectedCode.codeId)
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
                            if (selectedCodeIndex > 1) {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.SelectedQrCode(mainViewModel.selectedCode.codeId)
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
                    .background(MaterialTheme.colorScheme.onBackground)
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
                        text = "QR/Barcode",
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
                                .padding(vertical = 10.dp, horizontal = 22.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(
                                text = "Scan a code of a part of your morning routine",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 21.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 30.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.qr),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(300.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                        containerColor = if(isDarkMode) Color(
                                            0xff3F434F
                                        ) else Color.LightGray
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
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                            items(codesList) { qrCodeData ->
                                singleEntry(isDarkMode,
                                    qrCodeData = qrCodeData,
                                    isSelected = selectedCodeIndex == qrCodeData.codeId,
                                    changeSheetState = {
                                        clickedElement = qrCodeData.codeId
                                        filename = qrCodeData.qrCodeString
                                        configBottomBar = true
                                    }
                                ) {
                                    selectedCodeIndex = qrCodeData.codeId
                                    mainViewModel.updateSelectedCode(qrCodeData)
                                }
                            }
                        }
                    }
                }
                if (bottomSheetState) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            Log.d("BARCHK","$filename is code string and other us ${mainViewModel.detectedQrCodeState.qrCode.trim()}")
                            if (!updateAttempt) {
                                if (filename.isEmpty() && mainViewModel.detectedQrCodeState.qrCode.trim()
                                        .isNotEmpty()
                                ) {
                                    mainViewModel.insertCode(
                                        QrCodeData(
                                            codeId = System.currentTimeMillis(),
                                            qrCodeString = mainViewModel.detectedQrCodeState.qrCode
                                        )
                                    )
                                } else {
                                    mainViewModel.insertCode(
                                        QrCodeData(
                                            codeId = System.currentTimeMillis(),
                                            qrCodeString = filename
                                        )
                                    )
                                }
                            }
                            bottomSheetState = false
                        },
                        sheetState = sheetState,
                        dragHandle = {}) {
                        Column(
                            modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
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
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.73f),
                                    textAlign = TextAlign.End, fontWeight = FontWeight.W500
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    IconButton(onClick = {
                                        if (!updateAttempt) {
                                            if (filename.isEmpty() && mainViewModel.detectedQrCodeState.qrCode.trim()
                                                    .isNotEmpty()
                                            ) {
                                                mainViewModel.insertCode(
                                                    QrCodeData(
                                                        codeId = System.currentTimeMillis(),
                                                        qrCodeString = mainViewModel.detectedQrCodeState.qrCode
                                                    )
                                                )
                                            } else {
                                                mainViewModel.insertCode(
                                                    QrCodeData(
                                                        codeId = System.currentTimeMillis(),
                                                        qrCodeString = filename
                                                    )
                                                )
                                            }
                                        }
                                        bottomSheetState = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.surfaceTint
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)) {
                                BasicTextField(
                                    value = filename,
                                    onValueChange = {
                                        filename = it
                                    },
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f),
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.surfaceTint, fontSize = 16.sp),
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
                                    color = Color.LightGray,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }


                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp), contentAlignment = Alignment.Center
                            ) {
                                CustomButton(
                                    onClick = {
                                        if (updateAttempt) {
                                            mainViewModel.updateQrCode(
                                                QrCodeData(
                                                    codeId = clickedElement,
                                                    qrCodeString = filename
                                                )
                                            )
                                        } else {
                                            mainViewModel.insertCode(
                                                QrCodeData(
                                                    codeId = System.currentTimeMillis(),
                                                    qrCodeString = filename
                                                )
                                            )
                                        }
                                        clickedElement = -1
                                        filename = ""
                                        bottomSheetState = false
                                    },
                                    text = "Save",
                                    width = 0.8f,
                                    isEnabled = filename.trim().isNotEmpty()
                                )
                            }

                        }
                    }
                }
                if (configBottomBar) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            filename = ""
                            configBottomBar = false
                        },
                        sheetState = sheetState,
                        dragHandle = {}) {
                        Column(modifier = Modifier.background(MaterialTheme.colorScheme.onBackground)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.96f),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    scope.launch {
                                        sheetState.hide()
                                    }
                                    configBottomBar = false
                                    filename = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.surfaceTint
                                    )
                                }
                            }
                            if (selectedCodeIndex != clickedElement) {
                                singleSheetItem(
                                    name = "Delete Phrase",
                                    icon = Icons.Filled.Delete
                                ) {
                                    mainViewModel.deleteQrCode(clickedElement)
                                    scope.launch {
                                        configSheetState.hide()
                                    }
                                    configBottomBar = false
                                    filename = ""
                                }
                            }
                            singleSheetItem(name = "Edit Phrase", icon = Icons.Filled.Edit) {
                                updateAttempt = true
                                configBottomBar = false
                                bottomSheetState = true
                            }

                            Spacer(modifier = Modifier.height(30.dp))
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
    isDarkMode: Boolean,
    qrCodeData: QrCodeData,
    isSelected: Boolean,
    changeSheetState: () -> Unit,
    onQrCodeClicked: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(7.dp))
        Card(
            onClick = {
                onQrCodeClicked()
            },
            modifier = Modifier
                .height(62.dp)
                .padding(horizontal = 7.dp),
            shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
            border = BorderStroke(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xff0FAACB) else Color.Transparent
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected && isDarkMode) Color(0xff2F333E) else MaterialTheme.colorScheme.secondaryContainer)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = if (qrCodeData.qrCodeString.length > 20) "${qrCodeData.qrCodeString.take(19)}..." else qrCodeData.qrCodeString,

                    overflow = TextOverflow.Ellipsis,
                    color =MaterialTheme.colorScheme.surfaceTint,
                    fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 3.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                changeSheetState()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = if(isDarkMode)  R.drawable.dots else R.drawable.dotsblack),
                            contentDescription = "",
                            modifier = Modifier
                                .size(22.dp)
                        )
                    }
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