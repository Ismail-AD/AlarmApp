package com.appdev.alarmapp.ui.PreivewScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.alarmapp.Repository.AlarmRepository
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.RingtoneEntity
import com.appdev.alarmapp.utils.SystemRingtone
import com.appdev.alarmapp.utils.toRingtoneEntity
import com.appdev.alarmapp.utils.toSystemRingtoneEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingViewModel @Inject constructor(val repository: RingtoneRepository,val alarmRepository: AlarmRepository) : ViewModel() {

//
//    val recordingList: Flow<List<Ringtone>> = repository.roomDataFlow
//        .flowOn(Dispatchers.IO)
//
//    val ringtoneSystemList: Flow<List<Ringtone>> = repository.systemRings
//        .flowOn(Dispatchers.IO)
//
//    fun insertRingtone(ringtone: Ringtone) {
//        val ringtoneEntity = ringtone.toRingtoneEntity()
//        viewModelScope.launch {
//            repository.insertRingtone(ringtoneEntity)
//        }
//    }

    fun insertRingtone(systemRingtone: List<Ringtone>) {
        viewModelScope.launch {
            val systemRingtoneEntityList = systemRingtone.map { it.toSystemRingtoneEntity() }
            repository.insertRingtoneList(systemRingtoneEntityList)
        }
    }

    fun deleteNotes(id: Long) {
        viewModelScope.launch {
            repository.deleteRingtone(id)
        }
    }
}