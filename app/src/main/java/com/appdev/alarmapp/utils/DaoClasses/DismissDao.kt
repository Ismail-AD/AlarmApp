package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClass.DismissSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface DismissDao {
    @Query("SELECT * FROM dismiss_Setting_table")
    fun getDismissSettings(): Flow<DismissSettings>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDismissSettings(dismissSettings: DismissSettings)
    @Update
    suspend fun updateDismissSettings(dismissSettings: DismissSettings)
}