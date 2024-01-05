package com.appdev.alarmapp.ModelClass

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "dismiss_Setting_table")
data class DismissSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dismissTime: Int = 0,
    val missionTime: Int = 40,
    val photoSensitivity: String = "Normal",
    val muteTone: Boolean = true,
) : Parcelable
