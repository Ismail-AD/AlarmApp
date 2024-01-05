package com.appdev.alarmapp.Repository

import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.utils.DaoClasses.AlarmDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    val alarmDao: AlarmDao,
) {
    val roomDataFlow: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun insertAlarm(alarmEntity: AlarmEntity) {
        alarmDao.insertAlarm(alarmEntity)
    }

    suspend fun updateAlarm(alarmEntity: AlarmEntity) {
        alarmDao.updateData(alarmEntity)
    }


    suspend fun deleteAlarm(alarmId: Long) {
        alarmDao.deleteAlarmById(alarmId)
    }
}