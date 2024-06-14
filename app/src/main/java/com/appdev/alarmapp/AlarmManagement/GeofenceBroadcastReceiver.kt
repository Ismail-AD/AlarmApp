package com.appdev.alarmapp.AlarmManagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Currt","On receive called ")

        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) } ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        // Get the transition type.
        val listOfGeofences = geofencingEvent.triggeringGeofences
        listOfGeofences?.let { list ->
            for (geofence in list) {
                val transition = geofencingEvent.geofenceTransition
                val id = geofence.requestId

                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d("Currt","ENTER STATE IS CALLED !")

//                        context?.let {
//                            val intentENTER = Intent("userReached").apply {
//                                putExtra("idOfGeofence",id)
//                            }
//                            LocalBroadcastManager.getInstance(it).sendBroadcast(intentENTER)
//                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Log.d("Currt","OUTSIDE STATE IS CALLED !")
                        context?.let {
                            val intentEXIT = Intent("userReached").apply {
                                putExtra("idOfGeofence",id)
                            }
                            LocalBroadcastManager.getInstance(it).sendBroadcast(intentEXIT)
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_DWELL -> {}
                    else -> {}
                }
            }
        }
    }

}