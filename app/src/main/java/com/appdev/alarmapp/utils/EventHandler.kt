package com.appdev.alarmapp.utils

import java.time.LocalTime

sealed interface EventHandlerSearchBar {
    data class SearchBarOpen(val openSB: Boolean) : EventHandlerSearchBar
    data class SearchedData(val textToSearch: String) : EventHandlerSearchBar
    data class setNewList(val textToSearch: List<Ringtone>) : EventHandlerSearchBar
    object getOriginalList : EventHandlerSearchBar
}


sealed interface EventHandlerAlarm {
    data class getMilli(val timeInMilli: Long) : EventHandlerAlarm
    data class getDays(val days: Set<String>) : EventHandlerAlarm
    data class getTime(val time: LocalTime) : EventHandlerAlarm
    data class isActive(val isactive: Boolean) : EventHandlerAlarm
    data class isOneTime(val isOneTime: Boolean) : EventHandlerAlarm
    data class idAlarm(val iD: Long) : EventHandlerAlarm
    data class ringtone(val ringtone: Ringtone) : EventHandlerAlarm
    data class requestCode(val reqCode: Int) : EventHandlerAlarm
    data class getSnoozeTime(val getSnoozeTime: Int) : EventHandlerAlarm
    data class getMissions(val missions:List<Missions>) : EventHandlerAlarm
    object update : EventHandlerAlarm
}

sealed interface newAlarmHandler {
    data class getDays(val days: Set<String>) : newAlarmHandler
    data class getMilli(val timeInMilli: Long) : newAlarmHandler
    data class getTime(val time: LocalTime) : newAlarmHandler
    data class isActive(val isactive: Boolean) : newAlarmHandler
    data class isOneTime(val isOneTime: Boolean) : newAlarmHandler
    data class ringtone(val ringtone: Ringtone) : newAlarmHandler
    data class requestCode(val reqCode: Int) : newAlarmHandler
    data class getSnoozeTime(val getSnoozeTime: Int) : newAlarmHandler
    data class getMissions(val missions:List<Missions>) : newAlarmHandler
    object insert : newAlarmHandler

}


sealed interface MissionDemoHandler {
    data class checkMatch(val clickedIndex: Int) : MissionDemoHandler
    data class updateMatch(val matched: Boolean) : MissionDemoHandler
    data class GenerateAndStore(val size: Int) : MissionDemoHandler
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
    data class MissionId(val missionId: Int) : MissionDataHandler
    data class MissionLevel(val missionLevel: String) : MissionDataHandler
    data class MissionName(val missionName: String) : MissionDataHandler
    data class MissionProgress(val repeatProgress: Int) : MissionDataHandler
    data class IsSelectedMission(val isSelected: Boolean) : MissionDataHandler
    data class SelectedSentences(val setOfSentences:Set<CustomPhrase>) : MissionDataHandler
    data class AddCompleteMission(val setOfSentences:Set<CustomPhrase>,val repeat: Int,val missionId: Int,val missionLevel: String,val missionName: String,val repeatProgress: Int,val isSelected: Boolean) : MissionDataHandler
    data class AddList(val missionsList:List<Missions>) : MissionDataHandler
    object SubmitData : MissionDataHandler
    object ResetData : MissionDataHandler
    object ResetList : MissionDataHandler
}


sealed interface isOldOrNew {
    data class isOld(val isOld: Boolean) : isOldOrNew
}


sealed interface whichMissionHandler {
    data class thisMission(val missionMath: Boolean,val missionShake:Boolean,val missionMemory:Boolean,val isSteps:Boolean) : whichMissionHandler
}


sealed interface Updating {
    data class UpdateIt(val updateNow: Boolean) : Updating
}
