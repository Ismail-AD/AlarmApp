package com.appdev.alarmapp.ModelClasses

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appdev.alarmapp.utils.Missions
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "missions_table")
data class missionsEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0L,
    var listOfMissions: List<Missions> = emptyList(),
): Parcelable
