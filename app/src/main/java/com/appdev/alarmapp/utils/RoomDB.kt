package com.appdev.alarmapp.utils

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClass.Emergency_counter
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.utils.DaoClasses.AlarmBasicSettingDao
import com.appdev.alarmapp.utils.DaoClasses.AlarmDao
import com.appdev.alarmapp.utils.DaoClasses.DefaultSettingsDao
import com.appdev.alarmapp.utils.DaoClasses.DismissDao
import com.appdev.alarmapp.utils.DaoClasses.EmergencyCounterDao
import com.appdev.alarmapp.utils.DaoClasses.ImageStoreDao
import com.appdev.alarmapp.utils.DaoClasses.MissionsDao
import com.appdev.alarmapp.utils.DaoClasses.PhraseDao
import com.appdev.alarmapp.utils.DaoClasses.QrCodeDao
import com.appdev.alarmapp.utils.DaoClasses.RecordingsDao
import com.appdev.alarmapp.utils.DaoClasses.locationNameDao

@Database(
    entities = [RingtoneEntity::class, AlarmEntity::class, SystemRingtone::class, CustomPhrase::class, ImageData::class, QrCodeData::class, DefaultSettings::class, AlarmSetting::class, DismissSettings::class, missionsEntity::class, LocationByName::class, Emergency_counter::class],
    version = 40,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun phraseDao(): PhraseDao
    abstract fun emergencyCounterDao(): EmergencyCounterDao
    abstract fun locationByNameDao(): locationNameDao
    abstract fun recordingDao(): RecordingsDao
    abstract fun alarmDao(): AlarmDao
    abstract fun imageDao(): ImageStoreDao
    abstract fun qrCodeDao(): QrCodeDao
    abstract fun dSettingsDao(): DefaultSettingsDao
    abstract fun basicSettingsDao(): AlarmBasicSettingDao
    abstract fun dismissSettingsDao(): DismissDao
    abstract fun missionsDao(): MissionsDao
}