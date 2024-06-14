package com.appdev.alarmapp.ModelClass

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "basic_set_table")
data class AlarmSetting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val showInNotification: Boolean = false,
    val activeSort: Boolean = true,
    val preventUninstall: Boolean = false,
    val preventPhoneOff:Boolean = false
)
