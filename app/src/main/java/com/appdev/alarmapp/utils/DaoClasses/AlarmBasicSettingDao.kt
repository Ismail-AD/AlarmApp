package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClass.AlarmSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmBasicSettingDao {
    @Query("SELECT * FROM basic_set_table")
    fun getAlarmSettings(): Flow<AlarmSetting>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmSettings(alarmSettingEntity: AlarmSetting)
    @Update
    suspend fun updateAlarmSettings(alarmSettingEntity: AlarmSetting)
}