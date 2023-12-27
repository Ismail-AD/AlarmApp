package com.appdev.alarmapp.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrase_table")
data class CustomPhrase(
    @PrimaryKey(autoGenerate = true)
    val phraseId: Long = 0,
    val phraseData: String,
)
