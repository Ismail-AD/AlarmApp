package com.appdev.alarmapp.utils

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class Ringtone(
    val name: String = "Alarm Bell",
    val rawResourceId: Int = -1,
    val category: String = "",
    val uri: Uri? = null,
    val file: File? = null,
    val ringId: Long = -1
) : Parcelable