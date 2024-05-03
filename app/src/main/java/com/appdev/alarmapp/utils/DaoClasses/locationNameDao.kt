package com.appdev.alarmapp.utils.DaoClasses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appdev.alarmapp.utils.LocationByName
import com.appdev.alarmapp.utils.QrCodeData
import kotlinx.coroutines.flow.Flow

@Dao
interface locationNameDao {
    @Query("SELECT * FROM loc_table")
    fun getAllLocationByName(): Flow<List<LocationByName>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationByName(locationByName: LocationByName)

    @Query("DELETE FROM loc_table WHERE locId = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("SELECT * FROM loc_table WHERE locId = :id")
    suspend fun getLocationById(id: Long): LocationByName?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLocationByName(locationByName: LocationByName)
}