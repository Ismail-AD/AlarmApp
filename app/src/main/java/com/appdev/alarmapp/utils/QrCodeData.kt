package com.appdev.alarmapp.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qrCode_table")
data class QrCodeData(
    @PrimaryKey(autoGenerate = true)
    val codeId: Long = 0,
    val qrCodeString: String,
)
