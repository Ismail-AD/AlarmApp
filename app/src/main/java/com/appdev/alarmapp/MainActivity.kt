package com.appdev.alarmapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.AlarmManagement.SnoozeHandler
import com.appdev.alarmapp.AlarmManagement.Utils
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.navigation.navGraph
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.Helper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManagement: TokenManagement

    @Inject
    lateinit var textToSpeech: TextToSpeech
    val mainViewModel by viewModels<MainViewModel>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        val utils = Utils(this)

        if (Helper.isPlaying()) {
            Helper.stopStream()
        }


        lifecycleScope.launch {
            val closestSnoozeTimer = utils.findClosestSnoozeTimer()
            Log.d("CHKSM", "timer fetched at MAIN ----- $closestSnoozeTimer")
            if (closestSnoozeTimer != null && !utils.areSnoozeTimersEmpty()
            ) {
                Log.d("CHKSM", "LIST OF ALARM is not empty at main")
                mainViewModel.getAlarmById(closestSnoozeTimer.alarmId)
                mainViewModel.snoozedAlarm.collect { alarmEnt ->
                    Log.d("CHKSM", "Alarm snoozed id at main ${alarmEnt.id}")

                    if (alarmEnt.id != 0L && utils.getSnoozeTimerById(alarmEnt.id) != null) {
                        val newIntent = Intent(this@MainActivity, SnoozeHandler::class.java)
                        newIntent.putExtra("Alarm", alarmEnt)
                        newIntent.putExtra(
                            "notify",
                            mainViewModel.basicSettings.value.showInNotification
                        )
                        newIntent.putExtra("dismissSet", mainViewModel.dismissSettings.value)
                        startActivity(newIntent)
                        finish()
                    }
                }
            } else {
                setContent {
                    AlarmAppTheme {
                        val controller = rememberNavController()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager: AlarmManager =
                                getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            val hasPermission: Boolean = alarmManager.canScheduleExactAlarms()
                            if (hasPermission) {
                                navGraph(
                                    textToSpeech,
                                    controller,
                                    tokenManagement,
                                    applicationContext
                                )
                            } else {
                                val intent = Intent().apply {
                                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                }
                                startActivity(intent)
                            }
                        } else {
                            navGraph(textToSpeech, controller, tokenManagement, applicationContext)
                        }
                    }
                }

//            }
            }

        }


        @Composable
        fun Greeting(name: String, modifier: Modifier = Modifier) {
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        Greeting("Android")
//    }
//}