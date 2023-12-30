package com.appdev.alarmapp.ui.PreivewScreen

import android.Manifest
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.utils.AudioRecorder
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.newAlarmHandler
import com.appdev.alarmapp.utils.ringtoneList
import com.appdev.alarmapp.utils.tabs
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun RingtoneSelection(
    controller: NavHostController,
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current
    var selectedTabIndex by remember {
        mutableIntStateOf(0) // or use mutableStateOf(0)
    }
    val pagerState = rememberPagerState {
        tabs.size
    }
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val notifyPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showRationale by remember(notifyPermissionState) {
        mutableStateOf(false)
    }
    var showSheetState by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    var bottomSheetState by remember {
        mutableStateOf(false)
    }
    var recordingStarted by remember {
        mutableStateOf(false)
    }
    var elapsedTime by remember { mutableStateOf(0) }
    var recordingJob: Job? = null

    var showDialog by remember {
        mutableStateOf(false)
    }
    var filename by remember {
        mutableStateOf("")
    }
    var audioFile by remember {
        mutableStateOf<File?>(null)
    }
    var showToast by remember {
        mutableStateOf(false)
    }

    var loading by remember { mutableStateOf(false) }
    var selectedRingtone by remember { mutableStateOf( if (mainViewModel.whichAlarm.isOld) mainViewModel.selectedDataAlarm.ringtone else mainViewModel.newAlarm.ringtone) }

    val recordingsList by mainViewModel.recordingsList.collectAsStateWithLifecycle(initialValue = emptyList())
    val ringtoneDeviceList by mainViewModel.ringtoneSystemList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(ringtoneDeviceList) {
//        loading = ringtoneDeviceList.isEmpty()
        if (!loading) {
            selectedTabIndex = when (selectedRingtone) {
                in recordingsList -> 2
                in ringtoneDeviceList -> 1
                in ringtoneList -> 0
                else -> 0 // Default to the first tab if not found in any list.
            }
        }
    }

    // change the tab item when current page is changed
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (Helper.isPlaying()) {
            Helper.stopStream()
        }
    }
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "File name should not be Empty !", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(selectedRingtone) {
        // Start playing the audio when the composable is first created
        selectedRingtone.let {
            if (mainViewModel.whichAlarm.isOld) {
                mainViewModel.updateHandler(EventHandlerAlarm.ringtone(ringtone = it))
            } else {
                mainViewModel.newAlarmHandler(newAlarmHandler.ringtone(ringtone = it))
            }
            if (it.rawResourceId != -1) {
                Helper.playStream(context, it.rawResourceId)
            } else if (it.uri != null) {
                Helper.playStream(context, uri = it.uri)
            } else if (it.file != null) {
                Helper.playFile(file = it.file, context)
            }
        }
        onDispose {
            // Stop playing the audio when the composable is destroyed
            Helper.stopStream()
        }
    }

    DisposableEffect(key1 = showSheetState) {
        onDispose {
            stopRecording {
                elapsedTime = 0
                recordingJob?.cancel()
                recordingStarted = false
            }
        }
    }

    Scaffold(topBar = {
        TopBarRT() {
            controller.navigate(Routes.Preview.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }) { pd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pd)
                .background(
                    elementBack
                )
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
            // tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 2.dp,
                containerColor = elementBack
            ) {
                // tab items
                tabs.forEachIndexed { index, item ->
                    Tab(
                        selected = (index == selectedTabIndex),
                        onClick = {
                            selectedTabIndex = index
                            // change the page when the tab is changed
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = item.name)
                        }, selectedContentColor = Color.White, unselectedContentColor = Color.White
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                // on below line we are specifying
                // the different pages.
                    page ->
                when (page) {

                    // on below line we are calling tab content screen
                    // and specifying data as Home Screen.
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 5.dp, vertical = 10.dp)
                        ) {
                            LazyColumn {
                                items(ringtoneList) { tone ->
                                    EachRingtone(
                                        isSelected = tone == selectedRingtone,
                                        ringtone = tone
                                    ) {
                                        selectedRingtone = tone
                                        if (tone.rawResourceId == -1) {
                                            Helper.stopStream()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // on below line we are calling tab content screen
                    // and specifying data as Shopping Screen.
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 5.dp, vertical = 10.dp)
                        ) {
                            LazyColumn {
                                items(
                                    ringtoneDeviceList,
                                    key = { toneUri -> toneUri.name }) { toneUri ->
                                    EachRingtone(
                                        isSelected = toneUri == selectedRingtone,
                                        ringtone = toneUri
                                    ) {
                                        selectedRingtone = toneUri
                                    }
                                }
                            }
                        }
                    }
                    // on below line we are calling tab content screen
                    // and specifying data as Settings Screen.
                    2 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 5.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(60.dp)
                                .clickable {
                                    if (notifyPermissionState.status.isGranted) {
                                        if (Helper.isPlaying()) {
                                            Helper.stopStream()
                                        }
                                        showSheetState = true
                                    }
                                    if (notifyPermissionState.status.shouldShowRationale) {
                                        showRationale = true
                                    } else {
                                        notifyPermissionState.launchPermissionRequest()
                                    }
                                },
                            border = BorderStroke(width = 1.dp, color = Color.White),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.Transparent),
                                contentAlignment = Alignment.Center
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
                                        tint = Color.White
                                    )
                                    Text(
                                        "Record Now", fontSize = 18.sp,
                                        letterSpacing = 0.sp,
                                        color = Color.White, textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            items(recordingsList) { recording ->
                                EachRecording(
                                    isSelected = recording == selectedRingtone,
                                    mainViewModel::deleteRecording,
                                    ringtone = recording
                                ) {
                                    selectedRingtone = recording
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showSheetState) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f))
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xff1C1F26))
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recorded sound",
                            color = Color.White,
                            fontSize = 21.sp,
                            modifier = Modifier.fillMaxWidth(0.73f),
                            textAlign = TextAlign.End, fontWeight = FontWeight.W500
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                if (recordingStarted) {
                                    showDialog = true
                                } else {
                                    showSheetState = false
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, bottom = 30.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%.2f", elapsedTime.toDouble()),
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = " / 30.00",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 17.sp, fontWeight = FontWeight.W500
                        )
                    }

                    Card(
                        onClick = {
                            recordingStarted = !recordingStarted
                            if (recordingStarted) {
                                startRecording(recordingJob) {
                                    filename = getDefaultFilename()
                                    File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                                        filename
                                    ).also {
                                        AudioRecorder.startRecording(it, context = context)
                                        audioFile = it
                                    }
                                    recordingJob = scope.launch {
                                        for (i in 0..30) {
                                            delay(1000)
                                            elapsedTime = i
                                        }
                                        // Stop recording after 30 seconds
                                        stopRecording {
                                            AudioRecorder.stopRecording()
                                            recordingJob?.cancel()
                                            elapsedTime = 0
                                            showSheetState = false
                                            showDialog = false
                                            bottomSheetState = true
                                        }
                                    }
                                }
                            } else {
                                stopRecording {
                                    AudioRecorder.stopRecording()
                                    recordingJob?.cancel()
                                    elapsedTime = 0
                                    bottomSheetState = true
                                    showSheetState = false
                                }
                            }
                        },
                        modifier = Modifier
                            .size(100.dp), shape = CircleShape, colors = CardDefaults.cardColors(
                            containerColor = Color(0xffBC243B)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recordingStarted) Icons.Filled.Pause else Icons.Filled.Mic,
                                contentDescription = "",
                                tint = Color.White, modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
        if (bottomSheetState) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (filename.isEmpty()) {
                        audioFile?.let {
                            mainViewModel.insertRecording(
                                Ringtone(
                                    file = it,
                                    name = getDefaultFilename()
                                )
                            )
                        }
                        showToast = false
                        bottomSheetState = false
                    } else {
                        audioFile?.let {
                            mainViewModel.insertRecording(
                                Ringtone(
                                    file = it,
                                    name = getDefaultFilename()
                                )
                            )
                        }
                        showToast = false
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
                            text = "Recorded sound name",
                            color = Color.White,
                            fontSize = 21.sp,
                            modifier = Modifier.fillMaxWidth(0.83f),
                            textAlign = TextAlign.End, fontWeight = FontWeight.W500
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                if (filename.isEmpty()) {
                                    showToast = true
                                } else {
                                    audioFile?.let {
                                        mainViewModel.insertRecording(
                                            Ringtone(
                                                file = it,
                                                name = getDefaultFilename()
                                            )
                                        )
                                    }
                                    showToast = false
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

                    TextField(
                        value = filename,
                        onValueChange = {
                            filename = it
                        },
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 30.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = Color(0xff0FAACB), // Set the cursor color here
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp), contentAlignment = Alignment.Center
                    ) {
                        CustomButton(onClick = {
                            if (filename.isEmpty()) {
                                showToast = true
                            } else {
                                audioFile?.let {
                                    mainViewModel.insertRecording(
                                        Ringtone(
                                            file = it,
                                            name = getDefaultFilename()
                                        )
                                    )
                                }
                                showToast = false
                                bottomSheetState = false
                            }

                        }, text = "Save", width = 0.9f)
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
                    Text(text = "Recording Audio Feature requires the following permissions to work:\n RECORD AUDIO")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRationale = false
                            notifyPermissionState.launchPermissionRequest()
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
    if (showDialog) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Dialog(onDismissRequest = {
                showDialog = false
                showSheetState = false
            }) {
                Column(
                    modifier = Modifier
                        .background(Color(0xff1C1F26), shape = RoundedCornerShape(5.dp))
                ) {
                    Text(
                        text = "Exit without saving the",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "recorded sound?",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(), fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            CustomButton(
                                onClick = {
                                    showDialog = false
                                },
                                text = "Cancel",
                                width = 0.40f,
                                backgroundColor = Color(0xff3F434F)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            CustomButton(
                                onClick = {
                                    showDialog = false
                                    showSheetState = false
                                },
                                text = "Exit",
                                width = 0.75f,
                            )
                        }
                    }
                }
            }
        }

    }

}

fun findRingtoneByName(name: String, allRingtones: List<Ringtone>): Ringtone? {
    return allRingtones.find { it.name == name }
}

private fun startRecording(
    recordingJob: Job?,
    startTheFlow: () -> Unit,
) {
    recordingJob?.cancel() // Cancel any existing recording job
    startTheFlow()

}

private fun stopRecording(codeToPerform: () -> Unit) {
    codeToPerform()
}

// Function to get the default filename
@RequiresApi(Build.VERSION_CODES.O)
private fun getDefaultFilename(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.now().format(formatter)
}

@Composable
fun EachRingtone(isSelected: Boolean, ringtone: Ringtone, onCLick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable {
                onCLick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = { onCLick() })
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = ringtone.name, color = Color.White)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarRT(onClick: () -> Unit) {
    TopAppBar(title = {
        Text(
            text = "Sound",
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth(0.5f), fontSize = 16.sp
        )
    }, navigationIcon = {
        IconButton(onClick = { onClick() }) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIos,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
    }, actions = {

    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = elementBack,
        navigationIconContentColor = Color.White
    )
    )
}

@Composable
fun EachRecording(
    isSelected: Boolean,
    deleteIt: (Long) -> Unit,
    ringtone: Ringtone,
    onCLick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable {
                onCLick()
            },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        RadioButton(selected = isSelected, onClick = { onCLick() })
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = ringtone.name, color = Color.White)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = {
                if (Helper.isPlaying()) {
                    Helper.stopStream()
                }
                deleteIt(ringtone.ringId)
            }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
            }
        }
    }
}


fun getRingtoneTitle(context: Context, ringtoneUri: Uri): String {
    val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
    return ringtone.getTitle(context) ?: "Unknown Ringtone"
}

