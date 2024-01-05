package com.appdev.alarmapp.ModelClasses

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.ringtoneList
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.LocalTime

@Entity(tableName = "alarm_table")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var timeInMillis: Long = 0,
    var localTime: LocalTime = LocalTime.now(),
    val listOfDays: Set<String> = emptySet(),
    val isActive: Boolean = true,
    val isOneTime: Boolean = false,
    var snoozeTime: Int = 5,
    @Embedded
    var ringtone: Ringtone = ringtoneList[1],
    var listOfMissions: List<Missions> = emptyList(),
    var reqCode: Int = 0
) {
    fun initializeWithDefaultSettings(defaultSettings: DefaultSettings) {
        this.ringtone = defaultSettings.ringtone
        this.snoozeTime = defaultSettings.snoozeTime
        this.listOfMissions = defaultSettings.listOfMissions
    }
}
