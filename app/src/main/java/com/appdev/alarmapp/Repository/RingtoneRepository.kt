package com.appdev.alarmapp.Repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appdev.alarmapp.utils.CustomPhrase
import com.appdev.alarmapp.utils.PhraseDao
import com.appdev.alarmapp.utils.RecordingsDao
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.RingtoneEntity
import com.appdev.alarmapp.utils.SystemRingtone
import com.appdev.alarmapp.utils.toRingtone
import com.appdev.alarmapp.utils.toRingtoneFromSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class RingtoneRepository @Inject constructor(
    private val recordingsDao: RecordingsDao,private val phraseDao: PhraseDao
) {
    val roomRecordings: Flow<List<Ringtone>> = recordingsDao.getAllRecordings()
        .map { list ->
            list.map {
                it.toRingtone()
            }
        }
        .flowOn(Dispatchers.IO)

    val systemRings: Flow<List<Ringtone>> = recordingsDao.getAllSystemRings().map { list ->
        list.map {
            it.toRingtoneFromSystem()
        }
    }.flowOn(Dispatchers.IO)

    val listOfCustomPhrases: Flow<List<CustomPhrase>> = phraseDao.getAllPhrases()

    suspend fun insertPhrase(customPhrase: CustomPhrase) {
        phraseDao.insertPhrase(customPhrase)
    }
    suspend fun updatePhrase(customPhrase: CustomPhrase) {
        phraseDao.updatePhrase(customPhrase)
    }

    suspend fun deletePhrase(phraseId: Long) {
        phraseDao.deletePhraseById(phraseId)
    }

    suspend fun insertRingtone(ringtoneEntity: RingtoneEntity) {
        recordingsDao.insertRecording(ringtoneEntity)
    }

    suspend fun insertRingtoneList(list: List<SystemRingtone>) {
        recordingsDao.insertSystemRings(list)
    }

    suspend fun deleteRingtone(ringtoneId: Long) {
        recordingsDao.deleteRingtoneById(ringtoneId)
    }
}