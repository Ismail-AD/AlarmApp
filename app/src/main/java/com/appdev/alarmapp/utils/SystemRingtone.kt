package com.appdev.alarmapp.utils

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "systemRings_table", primaryKeys = ["name"])
data class SystemRingtone(
    val name: String,
    val ringUri: String
)
