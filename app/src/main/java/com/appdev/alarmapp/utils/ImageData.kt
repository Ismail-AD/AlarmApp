package com.appdev.alarmapp.utils

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
data class ImageData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bitmap: Bitmap? = null
)
