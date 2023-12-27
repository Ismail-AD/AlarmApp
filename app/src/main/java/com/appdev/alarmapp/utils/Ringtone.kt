package com.appdev.alarmapp.utils

import android.net.Uri
import java.io.File
import java.io.Serializable

data class Ringtone(
    val name: String = "Alarm Bell",
    val rawResourceId: Int = -1,
    val category: String = "",
    val uri: Uri? = null, val file: File? = null, val ringId: Long = -1
):Serializable
