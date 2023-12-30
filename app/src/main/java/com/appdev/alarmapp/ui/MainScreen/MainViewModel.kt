package com.appdev.alarmapp.ui.MainScreen

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.Repository.AlarmRepository
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.utils.CustomPhrase
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.Updating
import com.appdev.alarmapp.utils.convertSetToString
import com.appdev.alarmapp.utils.convertStringToSet
import com.appdev.alarmapp.utils.isOldOrNew
import com.appdev.alarmapp.utils.motivationalPhrases
import com.appdev.alarmapp.utils.newAlarmHandler
import com.appdev.alarmapp.utils.ringtoneList
import com.appdev.alarmapp.utils.toRingtoneEntity
import com.appdev.alarmapp.utils.toSystemRingtoneEntity
import com.appdev.alarmapp.utils.whichMissionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    val ringtoneRepository: RingtoneRepository,
    val alarmRepository: AlarmRepository
) : ViewModel() {

    var selectedDataAlarm by mutableStateOf(AlarmEntity())
    var newAlarm by mutableStateOf(AlarmEntity())
    var whichAlarm by mutableStateOf(newOneOrNot())
    var UpdatingState by mutableStateOf(UpdateIt())
    var missionDetailsList by mutableStateOf(listOf<Missions>())
    var dummyMissionList by mutableStateOf(listOf<Missions>())
    var missionDetails by mutableStateOf(Missions())
    var isRealAlarm by mutableStateOf(false)
    var whichMission by mutableStateOf(MissionState())
    var selectedImage by mutableStateOf(ImageData())
    var flashLight by mutableStateOf(false)
    var barCodeSheetState by mutableStateOf(false)

    var sentencesList by mutableStateOf(setOf<CustomPhrase>())

    private val _uiState: MutableStateFlow<QrScanUIState> = MutableStateFlow(QrScanUIState())
    val uiState: StateFlow<QrScanUIState> = _uiState

    fun onQrCodeDetected(result: String) {
        _uiState.update { it.copy(detectedQR = result) }
    }

    fun onTargetPositioned(rect: Rect) {
        _uiState.update { it.copy(targetRect = rect) }
    }

    fun updateBarCodeSheetState(value: Boolean) {
        barCodeSheetState = value
    }

    fun updateFlash(value: Boolean) {
        flashLight = value
    }

    val alarmList: Flow<List<AlarmEntity>> = alarmRepository.roomDataFlow
        .flowOn(Dispatchers.IO)

    val recordingsList: Flow<List<Ringtone>> = ringtoneRepository.roomRecordings
        .flowOn(Dispatchers.IO)

    val ringtoneSystemList: Flow<List<Ringtone>> = ringtoneRepository.systemRings
        .flowOn(Dispatchers.IO)

    val phrasesList: Flow<List<CustomPhrase>> =
        ringtoneRepository.listOfCustomPhrases.flowOn(Dispatchers.IO)

    val imagesList: Flow<List<ImageData>> =
        ringtoneRepository.listOfClickedImages.flowOn(Dispatchers.IO)

    fun insertImage(imageData: List<ImageData>) {
        viewModelScope.launch {
            ringtoneRepository.insertImage(imageData)
        }
    }

    fun getImageById(imageId: Long) {
        viewModelScope.launch {
            ringtoneRepository.getImage(imageId)?.let {
                selectedImage = it
            }
        }
    }

    fun deleteImage(id: Long) {
        viewModelScope.launch {
            ringtoneRepository.deleteImage(id)
        }
    }

    fun insertPhrase(customPhrase: CustomPhrase) {
        viewModelScope.launch {
            ringtoneRepository.insertPhrase(customPhrase)
        }
    }

    fun deletePhrase(id: Long) {
        viewModelScope.launch {
            ringtoneRepository.deletePhrase(id)
        }
    }

    fun updatePhrase(customPhrase: CustomPhrase) {
        viewModelScope.launch {
            ringtoneRepository.updatePhrase(customPhrase)
        }
    }


    fun insertRecording(ringtone: Ringtone) {
        val ringtoneEntity = ringtone.toRingtoneEntity()
        viewModelScope.launch {
            ringtoneRepository.insertRingtone(ringtoneEntity)
        }
    }

    fun updateIsReal(isReal: Boolean) {
        isRealAlarm = isReal
    }

    fun insertSystemList(systemRingtone: List<Ringtone>) {
        viewModelScope.launch {
            val systemRingtoneEntityList = systemRingtone.map { it.toSystemRingtoneEntity() }
            ringtoneRepository.insertRingtoneList(systemRingtoneEntityList)
        }
    }

    fun deleteRecording(id: Long) {
        viewModelScope.launch {
            ringtoneRepository.deleteRingtone(id)
        }
    }

    fun whichMissionHandle(whichMissionHandler: whichMissionHandler) {
        when (whichMissionHandler) {
            is whichMissionHandler.thisMission -> whichMission = whichMission.copy(
                isMath = whichMissionHandler.missionMath,
                isShake = whichMissionHandler.missionShake,
                isMemory = whichMissionHandler.missionMemory, isSteps = whichMissionHandler.isSteps
            )
        }
    }

    fun updateSentenceList(sentences: Set<CustomPhrase>) {
        sentencesList = sentences
    }

    fun updateSelectedImage(imageData: ImageData) {
        selectedImage = imageData
    }


    fun getRandomSentence(): CustomPhrase {
        return if (sentencesList.isNotEmpty()) sentencesList.toList()
            .random() else motivationalPhrases[0]
    }

    fun updateHandler(eventHandlerAlarm: EventHandlerAlarm) {

        when (eventHandlerAlarm) {
            is EventHandlerAlarm.getDays -> selectedDataAlarm =
                selectedDataAlarm.copy(listOfDays = eventHandlerAlarm.days)

            is EventHandlerAlarm.getTime -> selectedDataAlarm =
                selectedDataAlarm.copy(localTime = eventHandlerAlarm.time)

            is EventHandlerAlarm.isActive -> selectedDataAlarm =
                selectedDataAlarm.copy(isActive = eventHandlerAlarm.isactive)

            is EventHandlerAlarm.idAlarm -> selectedDataAlarm =
                selectedDataAlarm.copy(id = eventHandlerAlarm.iD)

            is EventHandlerAlarm.getMilli -> selectedDataAlarm =
                selectedDataAlarm.copy(timeInMillis = eventHandlerAlarm.timeInMilli)

            is EventHandlerAlarm.ringtone -> selectedDataAlarm =
                selectedDataAlarm.copy(ringtone = eventHandlerAlarm.ringtone)

            is EventHandlerAlarm.requestCode -> selectedDataAlarm =
                selectedDataAlarm.copy(reqCode = eventHandlerAlarm.reqCode)

            is EventHandlerAlarm.isOneTime -> {
                selectedDataAlarm =
                    selectedDataAlarm.copy(isOneTime = eventHandlerAlarm.isOneTime)
            }

            EventHandlerAlarm.update -> {
                updateAlarm(selectedDataAlarm)
            }

            is EventHandlerAlarm.getMissions -> {
                selectedDataAlarm =
                    selectedDataAlarm.copy(listOfMissions = eventHandlerAlarm.missions)
            }

            is EventHandlerAlarm.getSnoozeTime -> selectedDataAlarm =
                selectedDataAlarm.copy(snoozeTime = eventHandlerAlarm.getSnoozeTime)
        }

    }

    fun newAlarmHandler(newAlarmHandler: newAlarmHandler) {
        when (newAlarmHandler) {
            is newAlarmHandler.getDays -> newAlarm =
                newAlarm.copy(listOfDays = newAlarmHandler.days)

            is newAlarmHandler.ringtone -> newAlarm =
                newAlarm.copy(ringtone = newAlarmHandler.ringtone)

            is newAlarmHandler.getTime -> newAlarm = newAlarm.copy(localTime = newAlarmHandler.time)
            is newAlarmHandler.isActive -> newAlarm =
                newAlarm.copy(isActive = newAlarmHandler.isactive)

            com.appdev.alarmapp.utils.newAlarmHandler.insert -> {
                insertAlarm(newAlarm)
            }

            is newAlarmHandler.getMilli -> newAlarm =
                newAlarm.copy(timeInMillis = newAlarmHandler.timeInMilli)

            is newAlarmHandler.requestCode -> newAlarm =
                newAlarm.copy(reqCode = newAlarmHandler.reqCode)

            is newAlarmHandler.isOneTime -> {
                newAlarm =
                    newAlarm.copy(isOneTime = newAlarmHandler.isOneTime)
            }

            is newAlarmHandler.getMissions -> {
                newAlarm =
                    newAlarm.copy(listOfMissions = newAlarmHandler.missions)
            }

            is newAlarmHandler.getSnoozeTime -> newAlarm =
                newAlarm.copy(snoozeTime = newAlarmHandler.getSnoozeTime)
        }

    }

    fun missionData(missionDataHandler: MissionDataHandler) {

        when (missionDataHandler) {
            is MissionDataHandler.MissionLevel -> missionDetails =
                missionDetails.copy(missionLevel = missionDataHandler.missionLevel)

            is MissionDataHandler.RepeatTimes -> missionDetails =
                missionDetails.copy(repeatTimes = missionDataHandler.repeat)

            is MissionDataHandler.MissionName -> missionDetails =
                missionDetails.copy(missionName = missionDataHandler.missionName)

            is MissionDataHandler.MissionProgress -> missionDetails =
                missionDetails.copy(repeatProgress = missionDataHandler.repeatProgress)

            is MissionDataHandler.IsSelectedMission -> {
                missionDetails = missionDetails.copy(isSelected = missionDataHandler.isSelected)
            }

            is MissionDataHandler.MissionId -> {
                missionDetails = missionDetails.copy(missionID = missionDataHandler.missionId)
            }

            MissionDataHandler.SubmitData -> {
                if (missionDetailsList.any { it.missionID == missionDetails.missionID }) {
                    val newlist = missionDetailsList.toMutableList()
                    val indexedValue =
                        missionDetailsList.indexOfFirst { it.missionID == missionDetails.missionID }

                    newlist[indexedValue] = missionDetails

                    missionDetailsList = newlist
                } else {
                    missionDetails = missionDetails.copy(missionID = Random.nextInt(1, 999999))
                    missionDetailsList = missionDetailsList + missionDetails
                }
                missionDetails = missionDetails.copy(
                    missionID = -1,
                    repeatTimes = 1,
                    repeatProgress = 1,
                    missionLevel = "Very Easy",
                    missionName = "",
                    isSelected = false, selectedSentences = ""
                )
                sentencesList = emptySet()
            }

            is MissionDataHandler.AddList -> {
                missionDetailsList = missionDataHandler.missionsList
                if (isRealAlarm) {
                    dummyMissionList = missionDetailsList
                }
            }

            MissionDataHandler.ResetList -> missionDetailsList = emptyList()
            is MissionDataHandler.AddCompleteMission -> missionDetails = missionDetails.copy(
                missionID = missionDataHandler.missionId,
                repeatTimes = missionDataHandler.repeat,
                missionLevel = missionDataHandler.missionLevel,
                missionName = missionDataHandler.missionName,
                repeatProgress = missionDataHandler.repeatProgress,
                isSelected = missionDataHandler.isSelected,
                selectedSentences = convertSetToString(missionDataHandler.setOfSentences),
                imageId = missionDataHandler.imageId
            )

            MissionDataHandler.ResetData -> missionDetails = missionDetails.copy(
                missionID = -1,
                repeatTimes = 1,
                repeatProgress = 1,
                missionLevel = "Very Easy",
                missionName = "", selectedSentences = "",
                isSelected = false, imageId = 0
            )

            is MissionDataHandler.SelectedSentences -> {
                missionDetails =
                    missionDetails.copy(selectedSentences = convertSetToString(missionDataHandler.setOfSentences))
            }

            is MissionDataHandler.ImageId -> missionDetails =
                missionDetails.copy(imageId = missionDataHandler.imageId)
        }
    }

    fun isOld(isOldOr: isOldOrNew) {
        when (isOldOr) {
            is isOldOrNew.isOld -> whichAlarm = whichAlarm.copy(isOld = isOldOr.isOld)
        }
    }


    fun doUpdate(updating: Updating) {
        when (updating) {
            is Updating.UpdateIt -> UpdatingState =
                UpdatingState.copy(shouldUpdate = updating.updateNow)
        }
    }

    fun insertAlarm(alarmEntity: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(alarmEntity)
        }
    }

    fun updateAlarm(alarmEntity: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarmEntity)
        }
    }

    fun deleteAlarm(id: Long) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(id)
        }
    }


    data class newOneOrNot(val isOld: Boolean = false)
    data class UpdateIt(val shouldUpdate: Boolean = false)
    data class MissionState(
        val isMath: Boolean = false,
        val isShake: Boolean = false,
        val isMemory: Boolean = false,
        val isSteps: Boolean = false
    )

    data class MissionData(
        val missionID: Int = -1,
        val repeatTimes: Int = 1,
        val missionLevel: Float = 0f,
        val missionName: String = "",
        val repeatProgress: Int = 1,
        val isSelected: Boolean = false
    )

    data class QrScanUIState(
        val loading: Boolean = false,
        val detectedQR: String = "",
        val targetRect: Rect = Rect.Zero,
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    )
}