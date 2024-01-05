package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appdev.alarmapp.utils.RingtoneEntity
import com.appdev.alarmapp.utils.SystemRingtone
import kotlinx.coroutines.flow.Flow


@Dao
interface RecordingsDao {
    @Query("SELECT * FROM recording_table")
    fun getAllRecordings(): Flow<List<RingtoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(ringtoneEntity: RingtoneEntity)

    @Query("DELETE FROM recording_table WHERE id = :id")
    suspend fun deleteRingtoneById(id: Long)

    @Query("SELECT * FROM systemRings_table")
    fun getAllSystemRings(): Flow<List<SystemRingtone>>

    @Query("DELETE FROM systemRings_table")
    suspend fun deleteAllRingtone()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemRings(systemRingtone: List<SystemRingtone>)
}