package com.appdev.alarmapp.ui.MissionDemos

import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.elementBack
import com.appdev.alarmapp.utils.CustomPhrase
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.basicPhrases
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.motivationalPhrases
import com.appdev.alarmapp.utils.sentenceTabs
import com.appdev.alarmapp.utils.tabs
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SentenceSelection(
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

    val sheetState = rememberModalBottomSheetState()
    var bottomSheetState by remember {
        mutableStateOf(false)
    }

    var showToast by remember {
        mutableStateOf(false)
    }
    var showFileSave by remember {
        mutableStateOf(false)
    }
    var updateAttempt by remember {
        mutableStateOf(false)
    }
    var filename by remember {
        mutableStateOf("")
    }
    var oldfilename by remember {
        mutableStateOf("")
    }
    var typingStarted by remember {
        mutableStateOf(filename.trim().isNotEmpty())
    }
    var showDialog by remember {
        mutableStateOf(false)
    }

    var selectedPhrases by remember {
        mutableStateOf(
            if(convertStringToSet(mainViewModel.missionDetails.selectedSentences).isNotEmpty()){
                convertStringToSet(mainViewModel.missionDetails.selectedSentences).intersect(
                    motivationalPhrases.toSet())
            }
            else if (mainViewModel.sentencesList.isNotEmpty()) {
                mainViewModel.sentencesList.intersect(motivationalPhrases.toSet())
            } else {
                setOf(motivationalPhrases[0])
            }
        )
    }
    var selectedBasicPhrases by remember {
        mutableStateOf(
            if(convertStringToSet(mainViewModel.missionDetails.selectedSentences).isNotEmpty()){
                convertStringToSet(mainViewModel.missionDetails.selectedSentences).intersect(basicPhrases.toSet())
            }
            else if (mainViewModel.sentencesList.isNotEmpty()) {
                mainViewModel.sentencesList.intersect(basicPhrases.toSet())
            } else {
                emptySet()
            }
        )
    }
    val phraseList by mainViewModel.phrasesList.collectAsStateWithLifecycle(initialValue = emptyList())
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = filename) {
        typingStarted = filename.trim().isNotEmpty()

    }
    LaunchedEffect(key1 = updateAttempt) {
        if (updateAttempt && oldfilename.trim().isEmpty()) {
            oldfilename = filename
        } else if (!updateAttempt) {
            oldfilename = ""
        }
    }


    var selectedMyPhrases by remember {
        mutableStateOf(
            if (mainViewModel.sentencesList.isNotEmpty()) {
                mainViewModel.sentencesList.intersect(phraseList.toSet())
            } else {
                emptySet()
            }
        )
    }
    LaunchedEffect(key1 = phraseList) {
        loading = phraseList.isEmpty()
        if (!loading) {
            selectedMyPhrases =
                if(convertStringToSet(mainViewModel.missionDetails.selectedSentences).isNotEmpty()){
                convertStringToSet(mainViewModel.missionDetails.selectedSentences).intersect(
                    phraseList.toSet())
            } else if (mainViewModel.sentencesList.isNotEmpty()) {
                mainViewModel.sentencesList.intersect(phraseList.toSet())
            } else {
                emptySet()
            }
        } else {
            loading = false
        }
    }

    var clickPhraseId by remember {
        mutableLongStateOf(0)
    }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
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
            Toast.makeText(context, "Phrase should not be Empty !", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(elementBack)
                .padding(vertical = 10.dp, horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                onClick = {
                    controller.navigate(Routes.TypeMissionScreen.route) {
                        popUpTo(controller.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                border = BorderStroke(1.dp, Color.White),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = "Select the sentences",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(end = 17.dp)
            )

            Text(
                text = "Ok",
                color = if (selectedPhrases.isNotEmpty() || selectedBasicPhrases.isNotEmpty() || selectedMyPhrases.isNotEmpty()) Color.White else Color.White.copy(
                    alpha = 0.5f
                ),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500,
                modifier = Modifier.clickable {
                    if (selectedPhrases.isNotEmpty() || selectedBasicPhrases.isNotEmpty() || selectedMyPhrases.isNotEmpty()) {
                        val combinedPhrases =
                            selectedPhrases + selectedBasicPhrases + selectedMyPhrases
                        mainViewModel.updateSentenceList(combinedPhrases)

                        controller.navigate(Routes.TypeMissionScreen.route) {
                            popUpTo(controller.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
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
                edgePadding = 5.dp,
                containerColor = elementBack
            ) {
                // tab items
                sentenceTabs.forEachIndexed { index, item ->
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
                            Text(text = item.name, color = Color.White, fontSize = 15.sp)
                        }, icon = {
                            val selectedList =
                                if (index == 0) selectedPhrases else if (index == 1) selectedBasicPhrases else selectedMyPhrases
                            if (selectedList.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            Color.Gray,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = selectedList.size.toString(),
                                        modifier = Modifier.padding(horizontal = 13.dp)
                                    )
                                }
                            }
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
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                val allPhrases = motivationalPhrases.toSet()
                                                selectedPhrases =
                                                    if (selectedPhrases == allPhrases) setOf() else allPhrases
                                            },
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedPhrases.size == motivationalPhrases.size,
                                            onCheckedChange = {
                                                val allPhrases = motivationalPhrases.toSet()
                                                selectedPhrases = if (it) allPhrases else setOf()
                                            }, colors = CheckboxDefaults.colors(
                                                uncheckedColor = Color(0xffB6BDCA),
                                                checkmarkColor = Color.White,
                                                checkedColor = Color(0xff18677E)
                                            )
                                        )
                                        Text(
                                            text = "Select All",
                                            modifier = Modifier
                                                .padding(start = 8.dp), color = Color.White
                                        )
                                    }
                                }
                                items(motivationalPhrases) { phrase ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {

                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedPhrases.contains(phrase),
                                            onCheckedChange = { selected ->
                                                val alreadySelected = selectedPhrases.toMutableSet()
                                                if (selected) {
                                                    alreadySelected.add(phrase)
                                                } else {
                                                    alreadySelected.remove(phrase)
                                                }
                                                selectedPhrases = alreadySelected
                                            },
                                            colors = CheckboxDefaults.colors(
                                                uncheckedColor = Color(0xffB6BDCA),
                                                checkmarkColor = Color.White,
                                                checkedColor = Color(0xff18677E)
                                            )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (phrase.phraseData.split(" ").size > 11) {
                                                phrase.phraseData.split(" ").subList(0, 11)
                                                    .joinToString(" ") + "..."
                                            } else {
                                                phrase.phraseData
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(8.dp), color = Color.White
                                        )
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
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                val allPhrases = basicPhrases.toSet()
                                                selectedBasicPhrases =
                                                    if (selectedBasicPhrases == allPhrases) setOf() else allPhrases
                                            },
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedBasicPhrases.size == basicPhrases.size,
                                            onCheckedChange = {
                                                val allPhrases = basicPhrases.toSet()
                                                selectedBasicPhrases =
                                                    if (it) allPhrases else setOf()
                                            }, colors = CheckboxDefaults.colors(
                                                uncheckedColor = Color(0xffB6BDCA),
                                                checkmarkColor = Color.White,
                                                checkedColor = Color(0xff18677E)
                                            )
                                        )
                                        Text(
                                            text = "Select All",
                                            modifier = Modifier
                                                .padding(start = 8.dp), color = Color.White
                                        )
                                    }
                                }
                                items(basicPhrases) { phrase ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {

                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedBasicPhrases.contains(phrase),
                                            onCheckedChange = { selected ->
                                                val alreadySelected =
                                                    selectedBasicPhrases.toMutableSet()
                                                if (selected) {
                                                    alreadySelected.add(phrase)
                                                } else {
                                                    alreadySelected.remove(phrase)
                                                }
                                                selectedBasicPhrases = alreadySelected
                                            },
                                            colors = CheckboxDefaults.colors(
                                                uncheckedColor = Color(0xffB6BDCA),
                                                checkmarkColor = Color.White,
                                                checkedColor = Color(0xff18677E)
                                            )
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (phrase.phraseData.split(" ").size > 11) {
                                                phrase.phraseData.split(" ").subList(0, 11)
                                                    .joinToString(" ") + "..."
                                            } else {
                                                phrase.phraseData
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(8.dp), color = Color.White
                                        )
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
                                    showFileSave = true
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
                                        "Create new", fontSize = 18.sp,
                                        letterSpacing = 0.sp,
                                        color = Color.White, textAlign = TextAlign.Center
                                    )
                                }
                            }

                        }

                        LazyColumn {
                            items(phraseList) { phrase ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable {

                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedMyPhrases.contains(phrase),
                                        onCheckedChange = { selected ->
                                            val alreadySelected =
                                                selectedMyPhrases.toMutableSet()
                                            if (selected) {
                                                alreadySelected.add(phrase)
                                            } else {
                                                alreadySelected.remove(phrase)
                                            }
                                            selectedMyPhrases = alreadySelected
                                        },
                                        colors = CheckboxDefaults.colors(
                                            uncheckedColor = Color(0xffB6BDCA),
                                            checkmarkColor = Color.White,
                                            checkedColor = Color(0xff18677E)
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (phrase.phraseData.split(" ").size > 11) {
                                            phrase.phraseData.split(" ").subList(0, 11)
                                                .joinToString(" ") + "..."
                                        } else {
                                            phrase.phraseData
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp), color = Color.White
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                clickPhraseId = phrase.phraseId
                                                filename = phrase.phraseData
                                                bottomSheetState = true
                                            }, contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.dots),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(23.dp)
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        if (bottomSheetState) {
            ModalBottomSheet(
                onDismissRequest = {
                    filename = ""
                    bottomSheetState = false

                },
                sheetState = sheetState,
                dragHandle = {}) {
                Column(modifier = Modifier.background(Color(0xff1C1F26))) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.96f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                sheetState.hide()
                            }
                            filename = ""
                            bottomSheetState = false
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                    singleSheetItem(name = "Delete Phrase", icon = Icons.Filled.Delete) {
                        mainViewModel.deletePhrase(clickPhraseId)
                        scope.launch {
                            sheetState.hide()
                        }
                        filename = ""
                        bottomSheetState = false
                    }
                    singleSheetItem(name = "Edit Phrase", icon = Icons.Filled.Edit) {
                        updateAttempt = true
                        bottomSheetState = false
                        showFileSave = true
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        if (showFileSave) {
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
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Phrase",
                            color = Color.White,
                            fontSize = 21.sp,
                            modifier = Modifier.fillMaxWidth(0.66f),
                            textAlign = TextAlign.End, fontWeight = FontWeight.W500
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                if (typingStarted) {
                                    showDialog = true
                                } else {
                                    showFileSave = false
                                    filename = ""
                                    updateAttempt = false
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
                            if (it.length <= 120) {
                                filename = it
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 5.dp),
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
                    Divider(
                        color = Color.Gray.copy(0.7f),
                        modifier = Modifier.padding(horizontal = 15.dp),
                        thickness = 1.dp
                    )
                    Text(
                        text = "${filename.length}/120",
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 10.dp),
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp), contentAlignment = Alignment.Center
                    ) {
                        CustomButton(onClick = {
                            if (filename.trim().isEmpty()) {
                                showToast = true
                            } else {
                                if (updateAttempt) {
                                    if (selectedMyPhrases.contains(
                                            CustomPhrase(
                                                phraseId = clickPhraseId,
                                                phraseData = oldfilename
                                            )
                                        )
                                    ) {
                                        val alreadySelected =
                                            selectedMyPhrases.toMutableSet()
                                        alreadySelected.remove(
                                            CustomPhrase(
                                                phraseId = clickPhraseId,
                                                phraseData = oldfilename
                                            )
                                        )

                                        mainViewModel.updatePhrase(
                                            CustomPhrase(
                                                phraseId = clickPhraseId,
                                                phraseData = filename
                                            )
                                        )
                                        alreadySelected.add(
                                            CustomPhrase(
                                                phraseId = clickPhraseId,
                                                phraseData = filename
                                            )
                                        )
                                        selectedMyPhrases = alreadySelected
                                        updateAttempt = false
                                    } else {
                                        mainViewModel.updatePhrase(
                                            CustomPhrase(
                                                phraseId = clickPhraseId,
                                                phraseData = filename
                                            )
                                        )
                                        updateAttempt = false
                                    }
                                } else {
                                    mainViewModel.insertPhrase(
                                        CustomPhrase(
                                            phraseId = System.currentTimeMillis(),
                                            phraseData = filename
                                        )
                                    )
                                }
                                showToast = false
                                showFileSave = false
                                filename = ""
                            }

                        }, text = "Save", width = 0.9f)
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
        if (showDialog) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Dialog(onDismissRequest = {
                    updateAttempt = false
                    showDialog = false
                    showFileSave = false
                    filename = ""
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
                            text = "phrase ?",
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
                                        showFileSave = false
                                        filename = ""
                                        updateAttempt = false
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
}

@Composable
fun singleSheetItem(name: String, icon: ImageVector, onCLick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCLick() }
            .padding(vertical = 10.dp, horizontal = 20.dp),
    ) {
        Icon(imageVector = icon, contentDescription = "", tint = Color.White.copy(alpha = 0.75f))
        Text(
            text = name,
            modifier = Modifier.padding(start = 10.dp),
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}