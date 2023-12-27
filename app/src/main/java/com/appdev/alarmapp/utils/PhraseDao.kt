package com.appdev.alarmapp.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrase_table")
    fun getAllPhrases(): Flow<List<CustomPhrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(customPhrase: CustomPhrase)

    @Query("DELETE FROM phrase_table WHERE phraseId = :id")
    suspend fun deletePhraseById(id: Long)

    @Update
    suspend fun updatePhrase(customPhrase: CustomPhrase)
}