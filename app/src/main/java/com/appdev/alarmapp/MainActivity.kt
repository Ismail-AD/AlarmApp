package com.appdev.alarmapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.navigation.navGraph
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.AlarmAppTheme
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.Ringtone
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManagement: TokenManagement
    val checkViewModel by viewModels<checkOutViewModel>()

    val MY_REQCODE =  (0..19992).random()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if(Helper.isPlaying()){
            Helper.stopStream()
        }
        if (tokenManagement.getToken().isNullOrEmpty()) {
            val context = this
            val ringViewModel by viewModels<MainViewModel>()
            lifecycleScope.launch(Dispatchers.IO) {
                val rings = getSystemRingtones(context)
                ringViewModel.insertSystemList(rings)
            }
        }
        setContent {
            AlarmAppTheme {
                val controller = rememberNavController()



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager: AlarmManager =
                        getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val hasPermission: Boolean = alarmManager.canScheduleExactAlarms()
                    if (hasPermission) {
                        navGraph(controller, tokenManagement) {
                            AutoResolveHelper.resolveTask(checkViewModel.getPaymentData(), this, MY_REQCODE)
                        }
                    } else {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        }
                        startActivity(intent)
                    }
                }else{
                    navGraph(controller, tokenManagement) {
                        AutoResolveHelper.resolveTask(checkViewModel.getPaymentData(), this, MY_REQCODE)
                    }
                }


            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (MY_REQCODE == requestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    val paymentData = data?.let(PaymentData::getFromIntent)
                    paymentData?.let(checkViewModel::setPaymentData)
                }

                RESULT_CANCELED -> {
                    // The user cancelled without selecting a payment method.
                }

                AutoResolveHelper.RESULT_ERROR -> {

                }
            }
        }
    }

    override fun onDestroy() {
        tokenManagement.removeDays()
        super.onDestroy()
    }

}


fun getRingtoneTitle(context: Context, ringtoneUri: Uri): String {
    val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
    return ringtone.getTitle(context) ?: "Unknown Ringtone"
}

fun getSystemRingtones(context: Context): List<Ringtone> {
    val ringtoneManager = RingtoneManager(context)
    ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

    val cursor = ringtoneManager.cursor
    val mergedRingtoneList = mutableListOf<Ringtone>()

    while (cursor.moveToNext()) {
        val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
        val ringtoneName = getRingtoneTitle(context, ringtoneUri)
        val mergedRingtone = Ringtone(ringtoneName, uri = ringtoneUri)
        mergedRingtoneList.add(mergedRingtone)
    }

    cursor.close()
    return mergedRingtoneList
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