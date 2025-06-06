package com.appdev.alarmapp.ui.MainScreen

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.alarmapp.Hilt.TokenManagement
import com.appdev.alarmapp.ModelClass.AlarmSetting
import com.appdev.alarmapp.ModelClass.DefaultSettings
import com.appdev.alarmapp.ModelClass.DismissSettings
import com.appdev.alarmapp.ModelClass.Emergency_counter
import com.appdev.alarmapp.ModelClasses.AlarmEntity
import com.appdev.alarmapp.ModelClasses.missionsEntity
import com.appdev.alarmapp.Repository.AlarmRepository
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.utils.CustomPhrase
import com.appdev.alarmapp.utils.DefaultSettingsHandler
import com.appdev.alarmapp.utils.EventHandlerAlarm
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.LocationByName
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.Missions
import com.appdev.alarmapp.utils.QrCodeData
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.Updating
import com.appdev.alarmapp.utils.convertMillisToLocalTime
import com.appdev.alarmapp.utils.convertSetToString
import com.appdev.alarmapp.utils.isOldOrNew
import com.appdev.alarmapp.utils.motivationalPhrases
import com.appdev.alarmapp.utils.newAlarmHandler
import com.appdev.alarmapp.utils.toRingtoneEntity
import com.appdev.alarmapp.utils.toSystemRingtoneEntity
import com.appdev.alarmapp.utils.whichMissionHandler
import com.appdev.alarmapp.utils.whichRangeMissionHandle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    val ringtoneRepository: RingtoneRepository,
    val alarmRepository: AlarmRepository,
    val tokenManagement: TokenManagement
) : ViewModel() {


    private var _defaultSettings = MutableStateFlow(DefaultSettings())
    val defaultSettings: StateFlow<DefaultSettings> get() = _defaultSettings


    private var _basicSettings = MutableStateFlow(AlarmSetting())
    val basicSettings: StateFlow<AlarmSetting> get() = _basicSettings

    private var _dismissSettings = MutableStateFlow(DismissSettings())
    val dismissSettings: StateFlow<DismissSettings> get() = _dismissSettings

    private var _themeSettings = MutableStateFlow(false)
    val themeSettings: StateFlow<Boolean> get() = _themeSettings

    var _emergencyCounter = MutableStateFlow(Emergency_counter())
    val eCounter: StateFlow<Emergency_counter> get() = _emergencyCounter

    private var _missions = MutableStateFlow(missionsEntity())
    val missions: StateFlow<missionsEntity> get() = _missions

    private var _alarmID = MutableStateFlow(0L)
    val alarmID: StateFlow<Long> get() = _alarmID

    init {
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.getDefaultSettings.collect {
                Log.d("CHKDI", "Received Default Setting : $it")
                _defaultSettings.value = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.getBasicSettings.collect {
                _basicSettings.value = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.getDismissSettings.collect {
                _dismissSettings.value = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            tokenManagement.getTheme().collect {
                _themeSettings.value = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.getCounter.collect {
                _emergencyCounter.value = it
            }
        }
    }

    var selectedDataAlarm by mutableStateOf(AlarmEntity())
    var whichAlarm by mutableStateOf(newOneOrNot())
    var UpdatingState by mutableStateOf(UpdateIt())
    var missionDetailsList by mutableStateOf(listOf<Missions>())
    var dummyMissionList by mutableStateOf(listOf<Missions>())
    var missionDetails by mutableStateOf(Missions())
    var isRealAlarm by mutableStateOf(false)
    var dummyListEmpty by mutableStateOf(false)
    var whichMission by mutableStateOf(MissionState())
    var whichRangeMission by mutableStateOf(RangeMissionState())
    var selectedImage by mutableStateOf(ImageData())
    var selectedCode by mutableStateOf(QrCodeData(qrCodeString = "", qrCodeName = ""))
    var selectedLocationByName by mutableStateOf(
        LocationByName(
            locationName = "",
            locationString = "", longitude = 0.0, latitude = 0.0
        )
    )
    var flashLight by mutableStateOf(false)
    var previewMode by mutableStateOf(false)
    var hasSnoozed by mutableStateOf(false)
    var isSnoozed by mutableStateOf(false)
    var oldDummyListSize by mutableStateOf(0)


    private val _snoozeTime = MutableStateFlow(0L) // Initial value is 0 milliseconds
    val snoozeTime: StateFlow<Long> = _snoozeTime.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation


    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation


    private val _isFetchingLocationName = MutableStateFlow(false)
    val isFetchingLocationName: StateFlow<Boolean> = _isFetchingLocationName

    suspend fun getLocationName(latitude: Double, longitude: Double, context: Context): String {
        _isFetchingLocationName.value = true
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    _isFetchingLocationName.value = false
                    addresses[0].getAddressLine(0) ?: "Unknown Location"
                } else {
                    _isFetchingLocationName.value = false
                    "Unknown Location"
                }
            } catch (e: Exception) {
                _isFetchingLocationName.value = false
                "Unknown Location"
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        _isFetchingLocation.value = true
        viewModelScope.launch {
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    _currentLocation.value = location
                    _isFetchingLocation.value = false
                }
            } catch (e: Exception) {
                _currentLocation.value = null
                _isFetchingLocation.value = false
            }
        }
    }

    fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
        Log.d("Currt", "Updated Location: ${_currentLocation.value}")
    }


    fun updateMyCurrentLocationToNull() {
        _currentLocation.value = null
    }

    // Function to update the snooze time
    fun updateSnoozeTime(newTime: Long) {
        _snoozeTime.value = newTime
    }

    fun updateDummyListEmptyness(yesOrNo: Boolean) {
        dummyListEmpty = yesOrNo
    }

    var managingDefault by mutableStateOf(false)

    var sentencesList by mutableStateOf(setOf<CustomPhrase>())

    val _uiState: MutableStateFlow<QrScanUIState> = MutableStateFlow(QrScanUIState())
    val uiState: StateFlow<QrScanUIState> = _uiState
    var detectedQrCodeState by mutableStateOf(ProcessingState())
    var locationNameToSave by mutableStateOf("")
    var longitude by mutableStateOf(0.0)
    var latitude by mutableStateOf(0.0)
    var newAlarm by mutableStateOf(getAlarmEntityWithDefaultSettings())
    fun sendFeedback(data: String, onComplete: (Boolean, String) -> Unit) {
        ringtoneRepository.sendFeedbackInfo(data) { DoneOrNot, Message ->
            onComplete(DoneOrNot, Message)
        }
    }

    fun previewModeUpdate(value: Boolean) {
        previewMode = value
    }

    fun updateAlarmID(id: Long) {
        _alarmID.value = id
    }

    fun snoozeUpdate(value: Boolean) {
        hasSnoozed = value
    }

    fun alarmIsSnoozed(value: Boolean) {
        isSnoozed = value
    }

    fun getAlarmEntityWithDefaultSettings(): AlarmEntity {
        val alarmEntity = AlarmEntity()
        alarmEntity.initializeWithDefaultSettings(defaultSettings.value)
        return alarmEntity
    }

    fun basicSettingsInsertion(alarmSettingEntity: AlarmSetting) {
        viewModelScope.launch {
            ringtoneRepository.insertBasicSettings(alarmSettingEntity)
        }
    }

    fun updateThemeSettings(themeUpdated: Boolean) {
        _themeSettings.value = themeUpdated
        viewModelScope.launch {
            tokenManagement.saveTheme(themeSettings.value)
        }
    }

    fun updateDismissSettings(dismissSettingsObj: DismissSettings) {
        _dismissSettings.value = dismissSettingsObj
        viewModelScope.launch {
            ringtoneRepository.updateDismissSettings(dismissSettings.value)
        }
    }

    fun updateCounter(emergencyCounter: Emergency_counter) {
        _emergencyCounter.value = emergencyCounter
        viewModelScope.launch {
            ringtoneRepository.updateCounterSettings(eCounter.value)
        }
    }


    fun updateBasicSettings(alarmSettingEntity: AlarmSetting) {
        _basicSettings.value = alarmSettingEntity
        viewModelScope.launch {
            ringtoneRepository.updateBasicSettings(basicSettings.value)
        }
    }

    fun dismissSettingsInsertion(dismissSettingsObj: DismissSettings) {
        viewModelScope.launch {
            ringtoneRepository.insertDismissSettings(dismissSettingsObj)
        }
    }

    fun updateDetectedString(processingState: ProcessingState) {
        detectedQrCodeState = processingState
    }

    fun updateLocationName(locationName: String, newlatitude: Double, newlongitude: Double) {
        locationNameToSave = locationName
        latitude = newlatitude
        longitude = newlongitude
    }

    fun onQrCodeDetected(result: String) {
        _uiState.update { it.copy(detectedQR = result) }
    }

    fun onTargetPositioned(rect: Rect) {
        _uiState.update { it.copy(targetRect = rect) }
    }


    fun updateFlash(value: Boolean) {
        flashLight = value
    }

    fun updateSelectedCode(qrCodeData: QrCodeData) {
        selectedCode = qrCodeData
    }

    fun updateSelectedLocationName(locationByName: LocationByName) {
        selectedLocationByName = locationByName
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

    val codesList: Flow<List<QrCodeData>> =
        ringtoneRepository.listOfQrCodes.flowOn(Dispatchers.IO)

    val locationList: Flow<List<LocationByName>> =
        ringtoneRepository.listOfLocationNames.flowOn(Dispatchers.IO)


    fun insertDefaultSettings(defaultSettings: DefaultSettings) {
        viewModelScope.launch {
            ringtoneRepository.insertDefaultSettings(defaultSettings)
        }
    }

    fun insertCounter(emergencyCounter: Emergency_counter) {
        viewModelScope.launch {
            ringtoneRepository.insertCounterSettings(emergencyCounter)
        }
    }

    fun deleteAllRingtones() {
        viewModelScope.launch {
            ringtoneRepository.deleteAllRingtones()
        }
    }

    fun insertLocationByName(locationByName: LocationByName) {
        viewModelScope.launch {
            ringtoneRepository.insertLocationByName(locationByName)
        }
    }

    fun getLocationById(codeId: Long) {
        viewModelScope.launch {
            ringtoneRepository.getLocationByName(codeId)?.let {
                selectedLocationByName = it
            }
        }
    }

    fun updateLocationByName(locationByName: LocationByName) {
        viewModelScope.launch {
            ringtoneRepository.updateLocationByName(locationByName)
        }
    }

    fun deleteLocationByName(id: Long) {
        viewModelScope.launch {
            ringtoneRepository.deleteLocationByName(id)
        }
    }


    fun insertCode(qrCodeData: QrCodeData) {
        viewModelScope.launch {
            ringtoneRepository.insertQrCode(qrCodeData)
        }
    }

    fun getCodeById(codeId: Long) {
        viewModelScope.launch {
            ringtoneRepository.getQrCode(codeId)?.let {
                selectedCode = it
            }
        }
    }

    fun updateQrCode(qrCodeData: QrCodeData) {
        viewModelScope.launch {
            ringtoneRepository.updateQrCode(qrCodeData)
        }
    }

    fun deleteQrCode(id: Long) {
        viewModelScope.launch {
            ringtoneRepository.deleteQrCode(id)
        }
    }

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

    fun getRealUpdate(): Boolean {
        return isRealAlarm
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
                isMemory = whichMissionHandler.missionMemory,
                isSteps = whichMissionHandler.isSteps,
                isSquat = whichMissionHandler.isSquat, isWalk = whichMissionHandler.isWalk
            )
        }
    }

    fun whichRangeMissionHandler(whichRangeMissionHandle: whichRangeMissionHandle) {
        when (whichRangeMissionHandle) {
            is whichRangeMissionHandle.thisMission -> whichRangeMission = whichRangeMission.copy(
                isNumbers = whichRangeMissionHandle.missionNumber,
                isAlphabets = whichRangeMissionHandle.missionAlphabet,
                orderAlphabet = whichRangeMissionHandle.missionOrderAlphabet,
                orderNumbers = whichRangeMissionHandle.missionOrderNumbers,
                orderShapes = whichRangeMissionHandle.missionOrderShapes
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

            is EventHandlerAlarm.GetWakeUpTime -> selectedDataAlarm =
                selectedDataAlarm.copy(wakeUpTime = eventHandlerAlarm.getWUTime)

            is EventHandlerAlarm.IsGentleWakeUp -> selectedDataAlarm =
                selectedDataAlarm.copy(isGentleWakeUp = eventHandlerAlarm.isGentleWakeUp)

            is EventHandlerAlarm.LoudEffect -> selectedDataAlarm =
                selectedDataAlarm.copy(isLoudEffect = eventHandlerAlarm.isLoudEffectOrNot)

            is EventHandlerAlarm.TimeReminder -> selectedDataAlarm =
                selectedDataAlarm.copy(isTimeReminder = eventHandlerAlarm.isTimeReminderOrNot)

            is EventHandlerAlarm.CustomVolume -> selectedDataAlarm =
                selectedDataAlarm.copy(customVolume = eventHandlerAlarm.customVolume)

            is EventHandlerAlarm.IsLabel -> selectedDataAlarm =
                selectedDataAlarm.copy(isLabel = eventHandlerAlarm.isLabelOrNot)

            is EventHandlerAlarm.LabelText -> selectedDataAlarm =
                selectedDataAlarm.copy(labelTextForSpeech = eventHandlerAlarm.getLabelText)

            is EventHandlerAlarm.Vibrator -> selectedDataAlarm =
                selectedDataAlarm.copy(willVibrate = eventHandlerAlarm.setVibration)

            is EventHandlerAlarm.skipAlarm -> selectedDataAlarm =
                selectedDataAlarm.copy(skipTheAlarm = eventHandlerAlarm.skipped)

            is EventHandlerAlarm.getNextMilli -> selectedDataAlarm =
                selectedDataAlarm.copy(nextTimeInMillis = eventHandlerAlarm.upcomingMilli)

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
                Log.d("CHKTM", "My alarm is: ${newAlarm} ")
                insertAlarm(newAlarm)
            }

            is newAlarmHandler.getMilli -> newAlarm =
                newAlarm.copy(timeInMillis = newAlarmHandler.timeInMilli)


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

            is newAlarmHandler.GetWakeUpTime -> newAlarm =
                newAlarm.copy(wakeUpTime = newAlarmHandler.getWUTime)

            is newAlarmHandler.IsGentleWakeUp -> newAlarm =
                newAlarm.copy(isGentleWakeUp = newAlarmHandler.isGentleWakeUp)

            is newAlarmHandler.LoudEffect -> newAlarm =
                newAlarm.copy(isLoudEffect = newAlarmHandler.isLoudEffectOrNot)

            is newAlarmHandler.TimeReminder -> newAlarm =
                newAlarm.copy(isTimeReminder = newAlarmHandler.isTimeReminderOrNot)

            is newAlarmHandler.CustomVolume -> newAlarm =
                newAlarm.copy(customVolume = newAlarmHandler.customVolume)

            is newAlarmHandler.IsLabel -> newAlarm =
                newAlarm.copy(isLabel = newAlarmHandler.isLabelOrNot)

            is newAlarmHandler.LabelText -> newAlarm =
                newAlarm.copy(labelTextForSpeech = newAlarmHandler.getLabelText)

            is newAlarmHandler.Vibrator -> newAlarm =
                newAlarm.copy(willVibrate = newAlarmHandler.setVibration)

            is newAlarmHandler.getNextMilli -> newAlarm =
                newAlarm.copy(nextTimeInMillis = newAlarmHandler.upcomingMilli)

            is newAlarmHandler.skipAlarm -> newAlarm =
                newAlarm.copy(skipTheAlarm = newAlarmHandler.skipAlarm)
        }

    }

    fun missionData(missionDataHandler: MissionDataHandler) {

        when (missionDataHandler) {
            is MissionDataHandler.AddCompleteMission -> missionDetails = missionDetails.copy(
                missionID = missionDataHandler.missionId,
                repeatTimes = missionDataHandler.repeat,
                missionLevel = missionDataHandler.missionLevel,
                missionName = missionDataHandler.missionName,
                repeatProgress = missionDataHandler.repeatProgress,
                isSelected = missionDataHandler.isSelected,
                selectedSentences = convertSetToString(missionDataHandler.setOfSentences),
                imageId = missionDataHandler.imageId,
                codeId = missionDataHandler.codeId,
                locId = missionDataHandler.locId,
                valuesToPick = missionDataHandler.valuesToPick
            )

            is MissionDataHandler.MissionLevel -> missionDetails =
                missionDetails.copy(missionLevel = missionDataHandler.missionLevel)

            is MissionDataHandler.RepeatTimes -> missionDetails =
                missionDetails.copy(repeatTimes = missionDataHandler.repeat)

            is MissionDataHandler.MissionName -> missionDetails =
                missionDetails.copy(missionName = missionDataHandler.missionName)

            is MissionDataHandler.MissionProgress -> {
                missionDetails =
                    missionDetails.copy(repeatProgress = missionDataHandler.repeatProgress)
            }

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
                    isSelected = false,
                    selectedSentences = "",
                    codeId = 0,
                    imageId = 0,
                    locId = 0,
                    difficultyLevel = "Normal Mode"
                )
                sentencesList = emptySet()
            }

            is MissionDataHandler.AddList -> {
                missionDetailsList = missionDataHandler.missionsList
                if (isRealAlarm || previewMode) {
                    dummyMissionList = missionDetailsList
                }
            }

            MissionDataHandler.ResetList -> missionDetailsList = emptyList()

            MissionDataHandler.ResetData -> missionDetails = missionDetails.copy(
                missionID = -1,
                repeatTimes = 1,
                repeatProgress = 1,
                missionLevel = "Very Easy",
                missionName = "", selectedSentences = "",
                isSelected = false, imageId = 0, codeId = 0
            )

            is MissionDataHandler.SelectedSentences -> {
                missionDetails =
                    missionDetails.copy(selectedSentences = convertSetToString(missionDataHandler.setOfSentences))
            }

            is MissionDataHandler.ImageId -> missionDetails =
                missionDetails.copy(imageId = missionDataHandler.imageId)

            is MissionDataHandler.SelectedQrCode -> {
                missionDetails =
                    missionDetails.copy(codeId = missionDataHandler.selectedCodeId)
            }

            is MissionDataHandler.DifficultyLevel -> missionDetails =
                missionDetails.copy(difficultyLevel = missionDataHandler.difficultyLevel)

            is MissionDataHandler.NumbersCount -> missionDetails =
                missionDetails.copy(valuesToPick = missionDataHandler.noOfValue)

            is MissionDataHandler.SelectedLocationID -> {
                missionDetails =
                    missionDetails.copy(locId = missionDataHandler.selectedLocId)
            }
        }
    }

    fun setDefaultSettings(defaultSettingsHandler: DefaultSettingsHandler) {
        when (defaultSettingsHandler) {

            DefaultSettingsHandler.UpdateDefault -> {
                viewModelScope.launch(Dispatchers.IO) {
                    ringtoneRepository.updateDefaultSettings(defaultSettings.value)
                    missionDetailsList = emptyList()
                }
            }

            is DefaultSettingsHandler.GoingToSetDefault -> managingDefault =
                defaultSettingsHandler.isDefault

            is DefaultSettingsHandler.GetNewObject -> _defaultSettings.value =
                defaultSettingsHandler.defaultSettings

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

//    fun getAlarmById(id: Long) {
//        viewModelScope.launch {
//            alarmRepository.getSpecificAlarm(id).collect { mayNullAlarm ->
//                mayNullAlarm?.let {
//                    Log.d("CHKSJ", "Alarm MISSION LIST AT VM --------- ${it.listOfMissions}")
//                    _snoozedAlarm.value = it
//                }
//            }
//        }
//    }


    fun getMissionsById(id: Long) {
        viewModelScope.launch {
            alarmRepository.getMissionsById(id).collect { missions ->
                missions?.let {
                    Log.d("CHKSJ", "Alarm MISSION LIST AT VM --------- ${it.listOfMissions}")
                    _missions.value = it
                }
            }
        }
    }

    fun updateMissionsData(missionsE: missionsEntity) {
        _missions.value = missionsE
    }

    fun insertMissions(missionsEntity: missionsEntity) {
        viewModelScope.launch {
            alarmRepository.insertMissions(
                missionsEntity
            )
        }
    }

    fun updateMissions(missionsEntity: missionsEntity) {
        viewModelScope.launch {
            alarmRepository.updateMissions(missionsEntity)
        }
    }

    fun deleteMissions(id: Long) {
        viewModelScope.launch {
            alarmRepository.deleteMissions(id)
        }
    }

    fun insertAlarm(alarmEntity: AlarmEntity) {
        viewModelScope.launch {
            _alarmID.value = alarmRepository.insertAlarm(alarmEntity)
            Log.d("CHKAI", "${_alarmID.value} in VM")

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
        val isSteps: Boolean = false, val isSquat: Boolean = false, val isWalk: Boolean = false
    )

    data class RangeMissionState(
        val isNumbers: Boolean = false,
        val isAlphabets: Boolean = false,
        val orderAlphabet: Boolean = false,
        val orderNumbers: Boolean = false,
        val orderShapes: Boolean = false,
    )

    data class ProcessingState(
        val qrCode: String = "",
        val startProcess: Boolean = true
    )

    data class QrScanUIState(
        val loading: Boolean = false,
        val detectedQR: String = "",
        val targetRect: Rect = Rect.Zero,
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    )

}