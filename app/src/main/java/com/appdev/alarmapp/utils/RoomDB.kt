package com.appdev.alarmapp.utils

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appdev.alarmapp.ModelClasses.AlarmEntity

@Database(
    entities = [RingtoneEntity::class, AlarmEntity::class, SystemRingtone::class, CustomPhrase::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun phraseDao(): PhraseDao
    abstract fun recordingDao(): RecordingsDao
    abstract fun alarmDao(): AlarmDao
}