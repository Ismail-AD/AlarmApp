package com.appdev.alarmapp.Repository

import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.utils.CustomPhrase
import com.appdev.alarmapp.utils.DaoClasses.AlarmBasicSettingDao
import com.appdev.alarmapp.utils.DaoClasses.DefaultSettingsDao
import com.appdev.alarmapp.utils.DaoClasses.DismissDao
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.DaoClasses.ImageStoreDao
import com.appdev.alarmapp.utils.DaoClasses.PhraseDao
import com.appdev.alarmapp.utils.DaoClasses.QrCodeDao
import com.appdev.alarmapp.utils.QrCodeData
import com.appdev.alarmapp.utils.DaoClasses.RecordingsDao
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.RingtoneEntity
import com.appdev.alarmapp.utils.SystemRingtone
import com.appdev.alarmapp.utils.toRingtone
import com.appdev.alarmapp.utils.toRingtoneFromSystem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class RingtoneRepository @Inject constructor(
    private val RealtimeDBObject: FirebaseDatabase,
    private val recordingsDao: RecordingsDao,
    private val phraseDao: PhraseDao,
    private val imageStoreDao: ImageStoreDao,
    private val qrCodeDao: QrCodeDao,
    private val defaultSettingsDao: DefaultSettingsDao,
    private val alarmBasicSettingDao: AlarmBasicSettingDao,
    private val dismissDao: DismissDao,
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
    val listOfClickedImages: Flow<List<ImageData>> = imageStoreDao.getAllImages()
    val listOfQrCodes: Flow<List<QrCodeData>> = qrCodeDao.getAllQrCodes()
    val getDefaultSettings: Flow<DefaultSettings> = defaultSettingsDao.getDefaultSettings()
    val getBasicSettings: Flow<AlarmSetting> = alarmBasicSettingDao.getAlarmSettings()
    val getDismissSettings: Flow<DismissSettings> = dismissDao.getDismissSettings()

    fun sendFeedbackInfo(
        username: String, onComplete: (Boolean, String) -> Unit,
    ) {
        RealtimeDBObject.reference.child("Suggestion")
            .setValue(username).addOnSuccessListener {
                onComplete(true, "Submitted Successfully !")
            }.addOnFailureListener {
                onComplete(false, it.localizedMessage!!)
            }
    }
    suspend fun updateDismissSettings(dismissSettings: DismissSettings) {
        dismissDao.updateDismissSettings(dismissSettings)
    }
    suspend fun insertDismissSettings(dismissSettings: DismissSettings) {
        dismissDao.insertDismissSettings(dismissSettings)
    }
    suspend fun updateBasicSettings(alarmSettingEntity: AlarmSetting) {
        alarmBasicSettingDao.updateAlarmSettings(alarmSettingEntity)
    }
    suspend fun insertBasicSettings(alarmSettingEntity: AlarmSetting) {
        alarmBasicSettingDao.insertAlarmSettings(alarmSettingEntity)
    }
    suspend fun updateDefaultSettings(defaultSettings: DefaultSettings) {
        defaultSettingsDao.updateDefaultSettings(defaultSettings)
    }
    suspend fun insertDefaultSettings(defaultSettings: DefaultSettings) {
        defaultSettingsDao.insertDefaultSettings(defaultSettings)
    }

    suspend fun updateQrCode(qrCodeData: QrCodeData) {
        qrCodeDao.updateQrCode(qrCodeData)
    }

    suspend fun insertQrCode(qrCodeData: QrCodeData) {
        qrCodeDao.insertQrCode(qrCodeData)
    }

    suspend fun deleteQrCode(codeId: Long) {
        qrCodeDao.deleteCodeById(codeId)
    }

    suspend fun deleteAllRingtones() {
        recordingsDao.deleteAllRingtone()
    }

    suspend fun getQrCode(qrCodeId: Long): QrCodeData? {
        return qrCodeDao.getCodeById(qrCodeId)
    }


    suspend fun insertImage(listOfImages: List<ImageData>) {
        imageStoreDao.insertImage(listOfImages)
    }

    suspend fun getImage(imageId: Long): ImageData? {
        return imageStoreDao.getImageById(imageId)
    }

    suspend fun deleteImage(imageId: Long) {
        imageStoreDao.deleteImageById(imageId)
    }

    suspend fun updatePhrase(customPhrase: CustomPhrase) {
        phraseDao.updatePhrase(customPhrase)
    }

    suspend fun insertPhrase(customPhrase: CustomPhrase) {
        phraseDao.insertPhrase(customPhrase)
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