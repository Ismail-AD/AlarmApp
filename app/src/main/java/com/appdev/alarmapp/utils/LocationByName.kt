package com.appdev.alarmapp.utils

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loc_table")
data class LocationByName(
    @PrimaryKey(autoGenerate = true)
    val locId: Long = 0,
    val locationString: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double
)
