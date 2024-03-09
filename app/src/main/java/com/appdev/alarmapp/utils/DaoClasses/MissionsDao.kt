package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClasses.missionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(missionsEntity: missionsEntity)

    @Query("DELETE FROM missions_table WHERE id = :id")
    suspend fun deleteMissionById(id: Long)

    @Update
    suspend fun updateMission(missionsE: missionsEntity)

    @Query("SELECT * FROM missions_table WHERE id = :id")
    fun getMissionById(id: Long): Flow<missionsEntity?>
}