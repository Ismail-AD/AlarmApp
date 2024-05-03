package com.appdev.alarmapp.Repository

import android.util.Log
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.utils.DaoClasses.AlarmDao
import com.appdev.alarmapp.utils.DaoClasses.MissionsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    val alarmDao: AlarmDao, val missionsDao: MissionsDao
) {
    val roomDataFlow: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    suspend fun getAllAlarmsAsync(): List<AlarmEntity> {
        return withContext(Dispatchers.IO) {
            alarmDao.getAllAlarmsInList()
        }
    }
    var alarmId: Long = 0L


    suspend fun insertAlarm(alarmEntity: AlarmEntity):Long {
        Log.d("CHECKR", "------INSERT ALARM IS CALLED----")
        return alarmDao.insertAlarm(alarmEntity)
    }

    suspend fun updateAlarm(alarmEntity: AlarmEntity) {
        Log.d("CHECKR", "------UPDATE ALARM IS CALLED----")
        alarmDao.updateData(alarmEntity)
    }


    suspend fun deleteAlarm(alarmId: Long) {
        Log.d("CHECKR", "------DELETE ALARM IS CALLED----")
        alarmDao.deleteAlarmById(alarmId)
    }

    fun getSpecificAlarm(id: Long): Flow<AlarmEntity?> {
        Log.d("CHECKR", "------GET ALARM BY ID IS CALLED----")
        return alarmDao.getAlarmById(id)
    }


    // LIST OF MISSIONS
    suspend fun insertMissions(missionsE: missionsEntity) {
        Log.d("CHECKR", "------INSERT ALARM IS CALLED----")
        missionsDao.insertMission(missionsE)
    }

    suspend fun updateMissions(missionsE: missionsEntity) {
        Log.d("CHECKR", "------UPDATE ALARM IS CALLED----")
        missionsDao.updateMission(missionsE)
    }


    suspend fun deleteMissions(mId: Long) {
        Log.d("CHECKR", "------DELETE ALARM IS CALLED----")
        missionsDao.deleteMissionById(mId)
    }

    fun getMissionsById(id: Long): Flow<missionsEntity?> {
        Log.d("CHECKR", "------GET ALARM BY ID IS CALLED----")
        return missionsDao.getMissionById(id)
    }


}