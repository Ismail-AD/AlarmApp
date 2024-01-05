package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.ModelClass.DefaultSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultSettingsDao {
    @Query("SELECT * FROM default_Setting_table")
    fun getDefaultSettings(): Flow<DefaultSettings>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultSettings(defaultSettings: DefaultSettings)
    @Update
    suspend fun updateDefaultSettings(defaultSettings: DefaultSettings)
}