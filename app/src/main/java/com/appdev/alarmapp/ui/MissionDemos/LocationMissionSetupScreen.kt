package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.NotificationScreen.openAppSettings
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.ui.theme.signatureBlue
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.LocationByName
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.QrCodeData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationMissionSetup(
    controller: NavHostController,
    mainViewModel: MainViewModel,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val isDarkMode by mainViewModel.themeSettings.collectAsState()
    val defaultSettings = mainViewModel.defaultSettings.collectAsStateWithLifecycle()
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    var currentState by remember { mutableStateOf(billingState.value) }


    var showRationale by remember(permissionState) {
        mutableStateOf(false)
    }
    var showDialog by remember(permissionState) {
        mutableStateOf(false)
    }
    var showMapStepDialog by remember(permissionState) {
        mutableStateOf(false)
    }
    var guideOrNot by remember {
        mutableStateOf(permissionState.status.isGranted)
    }
    var showNonSelectToast by remember(permissionState) {
        mutableStateOf(false)
    }
    var loading by remember { mutableStateOf(false) }
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            guideOrNot = permissionState.status.isGranted
        }
    }
    LaunchedEffect(key1 = billingState.value) {
        currentState = billingState.value
    }

    val backStackEntry = controller.currentBackStackEntryAsState()

    BackHandler {
        controller.navigate(Routes.MissionMenuScreen.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    var showEmptyToast by remember {
        mutableStateOf(false)
    }
    var clickedElement by remember {
        mutableLongStateOf(if (mainViewModel.missionDetails.locId > 1) mainViewModel.missionDetails.locId else if (mainViewModel.selectedLocationByName.locId > 1) mainViewModel.selectedLocationByName.locId else -1)
    }
    var configBottomBar by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    val configSheetState = rememberModalBottomSheetState()
    var bottomSheetState by remember {
        mutableStateOf(false)
    }
    var updateAttempt by remember {
        mutableStateOf(false)
    }
    var filename by remember {
        mutableStateOf(mainViewModel.locationNameToSave)
    }
    var codeName by remember {
        mutableStateOf(mainViewModel.locationNameToSave)
    }
    val locationsList by mainViewModel.locationList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var selectedLocIndex by remember { mutableLongStateOf(if (mainViewModel.selectedLocationByName.locId > 1) mainViewModel.selectedLocationByName.locId else if (mainViewModel.missionDetails.locId > 1) mainViewModel.missionDetails.locId else -1) }
    LaunchedEffect(key1 = locationsList) {
        loading = locationsList.isEmpty()
        delay(700)
        if (locationsList.isEmpty()) {
            loading = false
        }
    }
    LaunchedEffect(showEmptyToast) {
        if (showEmptyToast) {
            Toast.makeText(context, "Location short name should not be Empty !", Toast.LENGTH_SHORT)
                .show()
            showEmptyToast = false
        }
    }
    LaunchedEffect(showNonSelectToast) {
        if (showNonSelectToast) {
            Toast.makeText(
                context,
                "Location not selected! Please long tap and press 'yes' to save location from map",
                Toast.LENGTH_SHORT
            )
                .show()
            showNonSelectToast = false
        }
    }
    LaunchedEffect(mainViewModel.locationNameToSave) {
        if (mainViewModel.locationNameToSave.trim().isNotEmpty()) {
            bottomSheetState = true
            filename = mainViewModel.locationNameToSave
            codeName = mainViewModel.locationNameToSave
        }
    }
    LaunchedEffect(key1 = bottomSheetState) {
        if (!bottomSheetState && mainViewModel.locationNameToSave.trim().isNotEmpty()) {
            mainViewModel.updateLocationName("")
        }
    }
    BackHandler {
        controller.popBackStack()
    }


    //-----------------------------------CODE------------------------------

    Scaffold(bottomBar = {
        if (guideOrNot) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground)
                    .padding(vertical = 14.dp)
                    .fillMaxWidth()
            ) {
                CustomButton(
                    onClick = {
                        if (selectedLocIndex > 1) {
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(Routes.CameraRoutineScreen.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            showNonSelectToast = true
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
                            if (selectedLocIndex > 1) {

                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.SelectedLocationID(mainViewModel.selectedLocationByName.locId)
                                )
//                                if(currentState is BillingResultState.Success){
                                mainViewModel.missionData(MissionDataHandler.AddList(defaultSettings.value.listOfMissions))
//                                }
                                mainViewModel.missionData(
                                    MissionDataHandler.RepeatTimes(
                                        repeat = 1
                                    )
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
                                showNonSelectToast = true
                            }
                        } else {
//                            if (currentState !is BillingResultState.Success) {
//                                mainViewModel.missionData(MissionDataHandler.ResetList)
//                            }
                            if (selectedLocIndex > 1) {
                                mainViewModel.missionData(
                                    MissionDataHandler.IsSelectedMission(
                                        isSelected = true
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.RepeatTimes(
                                        repeat = 1
                                    )
                                )
                                mainViewModel.missionData(
                                    MissionDataHandler.SelectedLocationID(mainViewModel.selectedLocationByName.locId)
                                )
                                mainViewModel.missionData(MissionDataHandler.SubmitData)
                                controller.navigate(Routes.Preview.route) {
                                    popUpTo(controller.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            } else {
                                showNonSelectToast = true
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
                        .fillMaxWidth(0.71f)
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
                        text = "Destination",
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
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "1.Location Should be turned on\n\n2.Long Press on specific location at Map to save it\n\n3.Reach at same spot to clear the mission",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.googlemap),
                                contentDescription = "",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .width(270.dp)
                                    .height(270.dp)
                                    .scale(2.7f)
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
                            text = "Continue",
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
                                        if (isLocationEnabled(context)) {
                                            showMapStepDialog = true
                                        } else {
                                            showDialog = true
                                        }
                                    }, shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkMode) Color(
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
                            items(locationsList) { locData ->
                                singleEntryLocation(isDarkMode,
                                    qrCodeData = locData,
                                    isSelected = selectedLocIndex == locData.locId,
                                    changeSheetState = {
                                        clickedElement = locData.locId
                                        filename = locData.locationString
                                        codeName = locData.locationName
                                        configBottomBar = true
                                    }
                                ) {
                                    selectedLocIndex = locData.locId
                                    mainViewModel.updateSelectedLocationName(locData)
                                }
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
                            if (selectedLocIndex != clickedElement) {
                                singleSheetItem(
                                    name = "Delete location",
                                    icon = Icons.Filled.Delete
                                ) {
                                    mainViewModel.deleteLocationByName(clickedElement)
                                    scope.launch {
                                        configSheetState.hide()
                                    }
                                    configBottomBar = false
                                    filename = ""
                                }
                            }
                            singleSheetItem(name = "Edit location", icon = Icons.Filled.Edit) {
                                updateAttempt = true
                                configBottomBar = false
                                bottomSheetState = true
                            }

                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }

                if (bottomSheetState) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            if (!updateAttempt) {
                                if (codeName.isEmpty() && mainViewModel.locationNameToSave.trim()
                                        .isNotEmpty()
                                ) {
                                    mainViewModel.insertLocationByName(
                                        LocationByName(
                                            locId = System.currentTimeMillis(),
                                            locationString = filename,
                                            locationName = mainViewModel.locationNameToSave
                                        )
                                    )
                                } else {
                                    mainViewModel.insertLocationByName(
                                        LocationByName(
                                            locId = System.currentTimeMillis(),
                                            locationString = filename, locationName = codeName
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
                                    text = "Location name",
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.69f),
                                    textAlign = TextAlign.End, fontWeight = FontWeight.W500
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    IconButton(onClick = {
                                        if (!updateAttempt) {
                                            if (codeName.isEmpty() && mainViewModel.locationNameToSave.trim()
                                                    .isNotEmpty()
                                            ) {
                                                mainViewModel.insertLocationByName(
                                                    LocationByName(
                                                        locId = System.currentTimeMillis(),
                                                        locationString = filename,
                                                        locationName = mainViewModel.locationNameToSave
                                                    )
                                                )
                                            } else {
                                                mainViewModel.insertLocationByName(
                                                    LocationByName(
                                                        locId = System.currentTimeMillis(),
                                                        locationString = filename,
                                                        locationName = codeName
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
                                    value = codeName,
                                    onValueChange = {
                                        codeName = it
                                    },
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.surfaceTint),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f),
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
                                        if (codeName.trim().isNotEmpty()) {
                                            if (updateAttempt) {
                                                mainViewModel.updateLocationByName(
                                                    LocationByName(
                                                        locId = clickedElement,
                                                        locationString = filename,
                                                        locationName = codeName
                                                    )
                                                )
                                            } else {
                                                mainViewModel.insertLocationByName(
                                                    LocationByName(
                                                        locId = System.currentTimeMillis(),
                                                        locationString = filename,
                                                        locationName = codeName
                                                    )
                                                )
                                            }
                                            clickedElement = -1
                                            filename = ""
                                            codeName = ""
                                            bottomSheetState = false
                                        } else {
                                            showEmptyToast = true
                                        }
                                    },
                                    text = "Save",
                                    width = 0.8f,
                                    isEnabled = filename.trim().isNotEmpty()
                                )
                            }

                        }
                    }
                }
                if (showMapStepDialog) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Dialog(onDismissRequest = {
                            showMapStepDialog = false
                            controller.navigate(Routes.MapScreen.route) {
                                popUpTo(Routes.ReachLocationMissionScreen.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.onBackground,
                                        shape = RoundedCornerShape(10.dp)
                                    ), horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "1. Long tap/press on your choice location on Map ",
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp), fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "2. Tap 'Yes/No' according to your choice ",
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    fontWeight = FontWeight.Medium
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.locadialog),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(15.dp))
                                        .width(250.dp)
                                        .height(250.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 15.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(bottom = 20.dp)
                                    ) {
                                        CustomButton(
                                            onClick = {
                                                showMapStepDialog = false
                                                controller.navigate(Routes.MapScreen.route) {
                                                    popUpTo(Routes.ReachLocationMissionScreen.route) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            },
                                            text = "Okay",
                                            width = 0.8f,
                                            backgroundColor = Color(0xff3F434F)
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Text(
                                "Location Services Disabled",
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                        },
                        text = {
                            Text(
                                "Please enable location services and try again.",
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Open device settings to enable location
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                    showDialog = false
                                }
                            ) {
                                Text("Enable", color = MaterialTheme.colorScheme.surfaceTint)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDialog = false
                                }
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.surfaceTint)
                            }
                        }
                    )
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
                            Text(text = "The Application requires the following permissions to work:\n LOCATION_ACCESS")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRationale = false
                                    openAppSettings(context)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun singleEntryLocation(
    isDarkMode: Boolean,
    qrCodeData: LocationByName,
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
                containerColor = if (isSelected && isDarkMode) Color(0xff2F333E) else MaterialTheme.colorScheme.secondaryContainer
            )

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Name: " + if (qrCodeData.locationName.length > 20) "${
                            qrCodeData.locationName.take(
                                19
                            )
                        }..." else qrCodeData.locationName,

                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp)
                    )
                    Text(
                        text = "Value: " + if (qrCodeData.locationString.length > 20) "${
                            qrCodeData.locationString.take(
                                19
                            )
                        }..." else qrCodeData.locationString,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        fontSize = 16.sp, modifier = Modifier.padding(start = 13.dp, top = 5.dp)
                    )
                }
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
                            painter = painterResource(id = if (isDarkMode) R.drawable.dots else R.drawable.dotsblack),
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


fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}