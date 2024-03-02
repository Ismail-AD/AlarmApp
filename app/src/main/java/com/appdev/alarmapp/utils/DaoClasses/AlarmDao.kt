package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.utils.ImageData
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm_table")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmEntity: AlarmEntity)

    @Query("DELETE FROM alarm_table WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Update
    suspend fun updateData(alarmEntity: AlarmEntity)

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarmById(id: Long): Flow<AlarmEntity?>

}