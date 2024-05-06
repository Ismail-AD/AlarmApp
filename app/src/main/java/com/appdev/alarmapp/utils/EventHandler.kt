package com.appdev.alarmapp.utils

import com.appdev.alarmapp.ModelClass.DefaultSettings
import java.time.LocalTime

sealed interface EventHandlerSearchBar {
    data class SearchBarOpen(val openSB: Boolean) : EventHandlerSearchBar
    data class SearchedData(val textToSearch: String) : EventHandlerSearchBar
    data class setNewList(val textToSearch: List<Ringtone>) : EventHandlerSearchBar
    object getOriginalList : EventHandlerSearchBar
}


sealed interface EventHandlerAlarm {
    data class skipAlarm(val skipped: Boolean) : EventHandlerAlarm
    data class getMilli(val timeInMilli: Long) : EventHandlerAlarm
    data class getNextMilli(val upcomingMilli: Long) : EventHandlerAlarm
    data class getDays(val days: Set<String>) : EventHandlerAlarm
    data class getTime(val time: LocalTime) : EventHandlerAlarm
    data class isActive(val isactive: Boolean) : EventHandlerAlarm
    data class isOneTime(val isOneTime: Boolean) : EventHandlerAlarm
    data class idAlarm(val iD: Long) : EventHandlerAlarm
    data class ringtone(val ringtone: Ringtone) : EventHandlerAlarm
    data class getSnoozeTime(val getSnoozeTime: Int) : EventHandlerAlarm
    data class getMissions(val missions: List<Missions>) : EventHandlerAlarm

    data class IsGentleWakeUp(val isGentleWakeUp: Boolean) : EventHandlerAlarm
    data class LoudEffect(val isLoudEffectOrNot: Boolean) : EventHandlerAlarm
    data class TimeReminder(val isTimeReminderOrNot: Boolean) : EventHandlerAlarm
    data class GetWakeUpTime(val getWUTime: Int) : EventHandlerAlarm

    data class CustomVolume(val customVolume: Float) : EventHandlerAlarm
    data class Vibrator(val setVibration: Boolean) : EventHandlerAlarm
    data class IsLabel(val isLabelOrNot: Boolean) : EventHandlerAlarm
    data class LabelText(val getLabelText: String) : EventHandlerAlarm
    object update : EventHandlerAlarm
}

sealed interface newAlarmHandler {
    data class getDays(val days: Set<String>) : newAlarmHandler
    data class getMilli(val timeInMilli: Long) : newAlarmHandler
    data class getNextMilli(val upcomingMilli: Long) : newAlarmHandler
    data class getTime(val time: LocalTime) : newAlarmHandler
    data class isActive(val isactive: Boolean) : newAlarmHandler
    data class isOneTime(val isOneTime: Boolean) : newAlarmHandler
    data class skipAlarm(val skipAlarm: Boolean) : newAlarmHandler
    data class ringtone(val ringtone: Ringtone) : newAlarmHandler
    data class getSnoozeTime(val getSnoozeTime: Int) : newAlarmHandler
    data class getMissions(val missions: List<Missions>) : newAlarmHandler
    data class IsGentleWakeUp(val isGentleWakeUp: Boolean) : newAlarmHandler
    data class LoudEffect(val isLoudEffectOrNot: Boolean) : newAlarmHandler
    data class TimeReminder(val isTimeReminderOrNot: Boolean) : newAlarmHandler
    data class GetWakeUpTime(val getWUTime: Int) : newAlarmHandler

    data class CustomVolume(val customVolume: Float) : newAlarmHandler
    data class Vibrator(val setVibration: Boolean) : newAlarmHandler
    data class IsLabel(val isLabelOrNot: Boolean) : newAlarmHandler
    data class LabelText(val getLabelText: String) : newAlarmHandler
    object insert : newAlarmHandler

}

sealed interface DefaultSettingsHandler {

    data class GoingToSetDefault(val isDefault: Boolean) : DefaultSettingsHandler
    data class GetNewObject(val defaultSettings: DefaultSettings) : DefaultSettingsHandler
    object UpdateDefault : DefaultSettingsHandler
}


sealed interface MissionDemoHandler {
    data class checkMatch(val clickedIndex: Int) : MissionDemoHandler
    object ResetCorrectList : MissionDemoHandler
    data class CheckCharacterMatches(val clickedCharacter: Char) : MissionDemoHandler
    data class updateMatch(val matched: Boolean) : MissionDemoHandler
    data class GenerateAndStore(val size: Int) : MissionDemoHandler
    data class GenerateRangedAndStore(val howMany: Int) : MissionDemoHandler
    data class GenerateRangedAndStoreAlphabet(val howMany: Int) : MissionDemoHandler
    data class GenerateTotalRangedAndReStore(
        val selectedNumbers: List<Int>,
        val newNumbersToPick: Int
    ) : MissionDemoHandler

    data class GenerateTotalRangedAndReStoreAlphabets(
        val selectedAlphabets: List<Char>,
        val newAlphabetsToPick: Int
    ) : MissionDemoHandler

    object ResetData : MissionDemoHandler
}

sealed interface MissionMathDemoHandler {
    data class SubmitAnswer(val answer: Int) : MissionMathDemoHandler
    data class CheckMatch(val subAnswer: Int) : MissionMathDemoHandler
    data class UpdateMatch(val notMatched: Boolean) : MissionMathDemoHandler
    object ResetData : MissionMathDemoHandler
}


sealed interface MissionDataHandler {
    data class RepeatTimes(val repeat: Int) : MissionDataHandler
    data class NumbersCount(val noOfValue: Int) : MissionDataHandler
    data class MissionId(val missionId: Int) : MissionDataHandler
    data class ImageId(val imageId: Long) : MissionDataHandler
    data class MissionLevel(val missionLevel: String) : MissionDataHandler
    data class DifficultyLevel(val difficultyLevel: String) : MissionDataHandler
    data class MissionName(val missionName: String) : MissionDataHandler
    data class MissionProgress(val repeatProgress: Int) : MissionDataHandler
    data class IsSelectedMission(val isSelected: Boolean) : MissionDataHandler
    data class SelectedQrCode(val selectedCodeId: Long) : MissionDataHandler
    data class SelectedLocationID(val selectedLocId: Long) : MissionDataHandler
    data class SelectedSentences(val setOfSentences: Set<CustomPhrase>) : MissionDataHandler
    data class AddCompleteMission(
        val setOfSentences: Set<CustomPhrase>,
        val repeat: Int,
        val missionId: Int,
        val missionLevel: String,
        val missionName: String,
        val repeatProgress: Int,
        val isSelected: Boolean,
        val imageId: Long,
        val codeId: Long,
        val locId:Long,val valuesToPick:Int
    ) : MissionDataHandler

    data class AddList(val missionsList: List<Missions>) : MissionDataHandler
    object SubmitData : MissionDataHandler
    object ResetData : MissionDataHandler
    object ResetList : MissionDataHandler
}


sealed interface isOldOrNew {
    data class isOld(val isOld: Boolean) : isOldOrNew
}


sealed interface whichMissionHandler {
    data class thisMission(
        val missionMath: Boolean,
        val missionShake: Boolean,
        val missionMemory: Boolean,
        val isSteps: Boolean,
        val isSquat: Boolean,
        val isWalk: Boolean
    ) : whichMissionHandler
}

sealed interface whichRangeMissionHandle {
    data class thisMission(
        val missionNumber: Boolean,
        val missionAlphabet: Boolean,
        val missionOrderAlphabet: Boolean,
        val missionOrderNumbers: Boolean,
        val missionOrderShapes: Boolean
    ) : whichRangeMissionHandle
}


sealed interface Updating {
    data class UpdateIt(val updateNow: Boolean) : Updating
}
