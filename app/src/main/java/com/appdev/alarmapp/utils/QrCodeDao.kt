package com.appdev.alarmapp.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrCodeDao {
    @Query("SELECT * FROM qrCode_table")
    fun getAllQrCodes(): Flow<List<QrCodeData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCodeData)

    @Query("DELETE FROM qrCode_table WHERE codeId = :id")
    suspend fun deleteCodeById(id: Long)

    @Query("SELECT * FROM qrCode_table WHERE codeId = :id")
    suspend fun getCodeById(id: Long): QrCodeData?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateQrCode(qrCode: QrCodeData)
}