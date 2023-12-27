package com.appdev.alarmapp.ModelClasses

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.ringtoneList
import java.time.LocalTime

@Entity(tableName = "alarms_table_")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var timeInMillis: Long = 0,
    var localTime: LocalTime = LocalTime.now(),
    val listOfDays: Set<String> = emptySet(),
    val isActive: Boolean = true,
    val isOneTime: Boolean = false,
    val snoozeTime: Int = 5,
    @Embedded
    val ringtone: Ringtone = ringtoneList[1],
    val listOfMissions: List<Missions> = emptyList(),
    var reqCode: Int = 0
)
