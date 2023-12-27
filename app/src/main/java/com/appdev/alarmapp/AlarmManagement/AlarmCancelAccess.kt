package com.appdev.alarmapp.AlarmManagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.MainActivity
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.AlarmCancel.AlarmCancelScreen
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionViewer.MathMissionHandler
import com.appdev.alarmapp.ui.MissionViewer.MissionHandlerScreen
import com.appdev.alarmapp.ui.MissionViewer.ShakeDetectionScreen
import com.appdev.alarmapp.ui.MissionViewer.StepMission
import com.appdev.alarmapp.ui.MissionViewer.TypingMissionHandler
import com.appdev.alarmapp.ui.PreivewScreen.localTimeToMillis
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.Serializable
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AlarmCancelAccess : ComponentActivity() {
    val mainViewModel by viewModels<MainViewModel>()
    var localTime: LocalTime = LocalTime.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmAppTheme {
                mainViewModel.updateIsReal(true)
                val alarmScheduler = AlarmScheduler(applicationContext, mainViewModel)

                val snoozeTime = intent.getStringExtra("snooze")
                val timeInM = intent.getStringExtra("tInM")
                val id = intent.getStringExtra("id")
                val receivedLocalTimeString = intent?.getStringExtra("localTime")
                receivedLocalTimeString?.let { LTS ->
                    localTime = LocalTime.parse(LTS)
                }

                if (intent?.hasExtra("ringtoneObj") == true) {
                    val receivedRingtone = intent.getSerializableExtra("ringtoneObj") as? Ringtone
                    receivedRingtone?.let { ringtone ->

                        if (intent?.hasExtra("list") == true) {

                            val receivedList =
                                intent.getSerializableExtra("list") as? List<Missions>

                            receivedList?.let { listOfMissions ->

                                mainViewModel.missionData(MissionDataHandler.AddList(missionsList = listOfMissions))

                                AlarmNavGraph(intent, mainViewModel = mainViewModel) {
                                    if (mainViewModel.dummyMissionList.isEmpty()) {
                                        if (intent.hasExtra("listOfDays")) {
                                            val listOfDaysJson = intent.getStringExtra("listOfDays")

                                            // Convert the JSON string back to a Set
                                            val gson = Gson()
                                            val type = object : TypeToken<Set<String>>() {}.type
                                            val listOfDays =
                                                gson.fromJson<Set<String>>(listOfDaysJson, type)
                                            timeInM?.let { tIM ->
                                                id?.let { ID ->
                                                    val alarmEntity = AlarmEntity(
                                                        id = ID.toLong(),
                                                        snoozeTime = snoozeTime?.toInt() ?: 5,
                                                        timeInMillis = tIM.toLong(),
                                                        listOfMissions = listOfMissions,
                                                        listOfDays = listOfDays,
                                                        reqCode = (0..19992).random(),
                                                        ringtone = ringtone,
                                                        localTime = localTime
                                                    )
                                                    scheduleTheAlarm(alarmEntity, alarmScheduler)
                                                }
                                            }
                                        }
                                        startActivity(Intent(this, MainActivity::class.java))
                                        mainViewModel.updateIsReal(false)
                                        finish()
                                    }
                                }
                            }
                        } else {
                            AlarmNavGraph(intent, mainViewModel = mainViewModel) {
                                if (mainViewModel.dummyMissionList.isEmpty()) {
                                    if (intent.hasExtra("listOfDays")) {
                                        val listOfDaysJson = intent.getStringExtra("listOfDays")

                                        // Convert the JSON string back to a Set
                                        val gson = Gson()
                                        val type = object : TypeToken<Set<String>>() {}.type
                                        val listOfDays =
                                            gson.fromJson<Set<String>>(listOfDaysJson, type)
                                        timeInM?.let { tIM ->
                                            id?.let { ID ->
                                                val alarmEntity = AlarmEntity(
                                                    id = ID.toLong(),
                                                    snoozeTime = snoozeTime?.toInt() ?: 5,
                                                    timeInMillis = tIM.toLong(),
                                                    listOfDays = listOfDays,
                                                    reqCode = (0..19992).random(),
                                                    ringtone = ringtone,
                                                    localTime = localTime
                                                )
                                                scheduleTheAlarm(alarmEntity, alarmScheduler)
                                            }
                                        }
                                    }
                                    startActivity(Intent(this, MainActivity::class.java))
                                    mainViewModel.updateIsReal(false)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        Helper.stopStream()
        super.onDestroy()
    }
}

fun scheduleTheAlarm(
    alarmEntity: AlarmEntity,
    alarmScheduler: AlarmScheduler,
) {
    val selectedTimeMillis = localTimeToMillis(alarmEntity.localTime)

    if (alarmEntity.listOfDays.isNotEmpty()) {
        val calendar = Calendar.getInstance()

        val nextOccurrence = alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .filter { it > calendar.get(Calendar.DAY_OF_WEEK) }
            .minOrNull() ?: alarmEntity.listOfDays
            .map { getDayOfWeek(it) }
            .minOrNull()!!

        val daysUntilNextOccurrence = nextOccurrence - calendar.get(Calendar.DAY_OF_WEEK)

        if (daysUntilNextOccurrence < 0) {
            val correctDay = 7 - kotlin.math.abs(daysUntilNextOccurrence)
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(correctDay.toLong())
        } else if (daysUntilNextOccurrence == 0) {
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(7L)
        } else {
            calendar.timeInMillis =
                selectedTimeMillis + TimeUnit.DAYS.toMillis(daysUntilNextOccurrence.toLong())
        }

        alarmEntity.timeInMillis = calendar.timeInMillis

        val instant = Instant.ofEpochMilli(calendar.timeInMillis)
        val offsetTime = OffsetTime.ofInstant(instant, ZoneId.systemDefault())
        alarmEntity.localTime = offsetTime.toLocalTime()
        alarmEntity.reqCode = (0..19992).random()

        alarmScheduler.schedule(alarmEntity)

    }
}

private fun getDayOfWeek(day: String): Int {
    val daysOfWeek =
        listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    return daysOfWeek.indexOf(day.lowercase(Locale.ROOT)) + 1
}

@Composable
fun AlarmNavGraph(
    intent: Intent,
    controller: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel, alarmEnds: () -> Unit
) {
    NavHost(
        navController = controller,
        startDestination = Routes.PreviewAlarm.route,
    ) {
        composable(route = Routes.PreviewAlarm.route) {
            AlarmCancelScreen(controller, mainViewModel, intent) {
                alarmEnds()
            }
        }
        composable(route = Routes.MissionShakeScreen.route) {
            ShakeDetectionScreen(mainViewModel = mainViewModel, controller) {
                alarmEnds()
            }
        }
        composable(route = Routes.StepDetectorScreen.route) {
            StepMission(mainViewModel = mainViewModel, controller){
                alarmEnds()
            }
        }

        composable(route = Routes.TypingPreviewScreen.route) {
            TypingMissionHandler(mainViewModel = mainViewModel, controller = controller){
                alarmEnds()
            }
        }
        composable(route = Routes.MissionMathScreen.route) {
            MathMissionHandler(
                mainViewModel,
                missionLevel = mainViewModel.missionDetails.missionLevel,
                controller = controller
            ) {
                alarmEnds()
            }
        }
        composable(route = Routes.MissionScreen.route) {
            val sizeOfBlocks = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 3
                "Easy" -> 4
                "Normal" -> 5
                "Hard" -> 6
                else -> 3
            }
            val cubeHeightWidth = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 100.dp
                "Easy" -> 75.dp
                "Normal" -> 61.dp
                "Hard" -> 50.dp
                else -> 100.dp
            }
            val columnPadding = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 8.dp
                "Easy" -> 4.dp
                "Normal" -> 1.dp
                "Hard" -> 0.dp
                else -> 8.dp
            }
            val lazyRowHeight = when (mainViewModel.missionDetails.missionLevel) {
                "Very Easy" -> 100.dp
                "Easy" -> 80.dp
                "Normal" -> 65.dp
                "Hard" -> 55.dp
                else -> 100.dp
            }
            MissionHandlerScreen(
                cubeHeightWidth, columnPadding, lazyRowHeight,
                controller, totalSize = sizeOfBlocks,
                mainViewModel = mainViewModel
            ) {
                alarmEnds()
            }
        }
    }
}