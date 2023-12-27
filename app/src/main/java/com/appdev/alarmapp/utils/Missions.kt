package com.appdev.alarmapp.utils

import java.io.Serializable

data class Missions(
    val missionID: Int = -1,
    val repeatTimes: Int = 1,
    val missionLevel: String = "Very Easy",
    val missionName: String = "",
    val repeatProgress: Int = 1,
    val selectedSentences: String = "",
    val isSelected: Boolean = false
) : Serializable
