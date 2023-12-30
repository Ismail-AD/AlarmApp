package com.appdev.alarmapp.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageStoreDao {
    @Query("SELECT * FROM image_table")
    fun getAllImages(): Flow<List<ImageData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(listOfImages: List<ImageData>)

    @Query("DELETE FROM image_table WHERE id = :id")
    suspend fun deleteImageById(id: Long)

    @Query("SELECT * FROM image_table WHERE id = :id")
    suspend fun getImageById(id: Long): ImageData?
}