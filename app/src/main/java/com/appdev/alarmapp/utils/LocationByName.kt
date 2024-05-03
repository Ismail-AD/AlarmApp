package com.appdev.alarmapp.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loc_table")
data class LocationByName(
    @PrimaryKey(autoGenerate = true)
    val locId: Long = 0,
    val locationString: String,
    val locationName: String
)
