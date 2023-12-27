package com.appdev.alarmapp.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemRings(systemRingtone: List<SystemRingtone>)
}