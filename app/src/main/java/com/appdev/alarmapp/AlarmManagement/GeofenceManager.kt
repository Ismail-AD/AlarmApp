package com.appdev.alarmapp.AlarmManagement

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class GeofenceManager(context: Context) {
    private val TAG = "GeofenceManagement"
    private val CUSTOM_REQUEST_CODE_GEOFENCE = 24
    private val client = LocationServices.getGeofencingClient(context)
    private val _geofenceList = MutableStateFlow<Map<String, Geofence>>(emptyMap())
    val geofenceList: StateFlow<Map<String, Geofence>> = _geofenceList

    fun updateGeofenceList(list: Map<String, Geofence>) {
        _geofenceList.value = list
    }
    //  to handle the intents sent from Location Services when geofence transitions occur, you can define a PendingIntent
    private val geofencingPendingIntent: PendingIntent by lazy {
        // broadcast is a message that any other application component can receive.

        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            Intent(context,GeofenceBroadcastReceiver::class.java),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    fun addGeofence(
        key: String,
        location: LatLng,
        radiusInMeters: Float,
    ) {
        _geofenceList.value = _geofenceList.value.toMutableMap().apply {
            put(key, createGeofence(key, location, radiusInMeters))
        }
    }


    // To add geofences, use geofencing client method
    @SuppressLint("MissingPermission")
    fun registerGeofence() {
        client.addGeofences(createGeofencingRequest(), geofencingPendingIntent)
            .addOnSuccessListener {
                Log.d("Currt","registerGeofence: SUCCESS")
                Log.d(TAG, "registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                Log.d("Currt","registerGeofence: Failure\n" +
                        "$exception")
                Log.d(TAG, "registerGeofence: Failure\n$exception")

            }
    }

    fun deregisterGeofence() = kotlin.runCatching {
        Log.d("Currt","-----DEregisterGeofence: SUCCESS")
        client.removeGeofences(geofencingPendingIntent)
    }

    // Specifies the list of geofences to be monitored and how the geofence notifications should be reported.
    private fun createGeofencingRequest(): GeofencingRequest {
        Log.d("Currt","List of fences: ${geofenceList.value.values.toList()}")

        return GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_EXIT
            ) //This can be useful, for instance, if you want to know via notification
            // when a user is already inside/outside a geofenced area as soon as the geofence is added.
            .addGeofences(geofenceList.value.values.toList())
            .build()
    }

    // Create the geofence.
    private fun createGeofence(
        key: String,
        location: LatLng,
        radiusInMeters: Float,
    ): Geofence {
        Log.d("Currt","Creation of geofence on location : ${location.latitude} and Lon: ${ location.longitude}")

        return Geofence.Builder()
            .setRequestId(key) //This is a string to identify this
            // geofence.
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters).setExpirationDuration(7 * 24 * 60 * 60 * 1000)
            // removed after this period of time.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_ENTER ) //Alerts are only generated for these
            // transition.
            .build()
    }

}