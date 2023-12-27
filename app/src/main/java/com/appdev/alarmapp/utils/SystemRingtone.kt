package com.appdev.alarmapp.utils

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "systemRings_table")
data class SystemRingtone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ringUri: String
)
