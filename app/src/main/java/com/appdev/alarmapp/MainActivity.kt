package com.appdev.alarmapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.navigation.navGraph
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.Helper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManagement: TokenManagement

    @Inject
    lateinit var textToSpeech: TextToSpeech



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if(Helper.isPlaying()){
            Helper.stopStream()
        }

        setContent {
            AlarmAppTheme {
                val controller = rememberNavController()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager: AlarmManager =
                        getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val hasPermission: Boolean = alarmManager.canScheduleExactAlarms()
                    if (hasPermission) {
                        navGraph(textToSpeech,controller, tokenManagement,applicationContext)
                    } else {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        }
                        startActivity(intent)
                    }
                }else{
                    navGraph(textToSpeech, controller, tokenManagement, applicationContext)
                }


            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
    }

}




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AlarmAppTheme {
//        Greeting("Android")
//    }
//}