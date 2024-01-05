package com.appdev.alarmapp.ModelClass

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.ringtoneList

@Entity(tableName = "default_Setting_table")
data class DefaultSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val snoozeTime: Int = 5,
    @Embedded
    val ringtone: Ringtone = ringtoneList[1],
    val listOfMissions: List<Missions> = emptyList()
)