package com.appdev.alarmapp.ModelClass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emer_counter_table")
data class Emergency_counter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val counter_emer: Int = 100
)
