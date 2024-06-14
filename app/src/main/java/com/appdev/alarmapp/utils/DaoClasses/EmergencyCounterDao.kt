package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClass.Emergency_counter
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyCounterDao {
    @Query("SELECT * FROM emer_counter_table")
    fun getCounterData(): Flow<Emergency_counter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(emergencyCounter: Emergency_counter)

    @Query("DELETE FROM emer_counter_table WHERE id = :id")
    suspend fun deleteCounterById(id: Long)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCounterById(counter: Emergency_counter)
}