package com.appdev.alarmapp.ui.MissionViewer

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.appdev.alarmapp.utils.MissionDemoHandler
import com.appdev.alarmapp.utils.MissionMathDemoHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor() : ViewModel() {

    var missionHandler by mutableStateOf(MissionHandlerData())
    var missionMathHandler by mutableStateOf(MissionMathHandlerData())

    fun missionEventHandler(missionDemoHandler: MissionDemoHandler) {
        when (missionDemoHandler) {

            is MissionDemoHandler.checkMatch -> {
                if (missionHandler.preservedIndexes.contains(missionDemoHandler.clickedIndex)) {
                    missionHandler.correctChoiceList =
                        missionHandler.correctChoiceList + missionDemoHandler.clickedIndex
                    missionHandler =
                        missionHandler.copy(correctChoiceList = missionHandler.correctChoiceList)
                } else {
                    missionHandler = missionHandler.copy(
                        notMatched = true,
                        clicked = missionDemoHandler.clickedIndex
                    )
                }
            }

            is MissionDemoHandler.updateMatch -> {
                missionHandler = missionHandler.copy(notMatched = missionDemoHandler.matched)
            }


            MissionDemoHandler.ResetData -> missionHandler =
                missionHandler.copy(
                    preservedIndexes = emptyList(),
                    correctChoiceList = emptyList(),
                    notMatched = false,
                    clicked = -1,
                )

            is MissionDemoHandler.GenerateAndStore -> {
                if (missionHandler.preservedIndexes.isEmpty()) {
                    missionHandler =
                        missionHandler.copy(
                            preservedIndexes = generateRandomIndices(
                                missionDemoHandler.size
                            )
                        )
                }
            }
        }
    }


    fun missionMathEventHandler(missionMathDemoHandler: MissionMathDemoHandler) {
        when (missionMathDemoHandler) {
            MissionMathDemoHandler.ResetData -> missionMathHandler =
                missionMathHandler.copy(answer = 0, notMatched = false, answerCorrect = false)

            is MissionMathDemoHandler.SubmitAnswer -> {
                missionMathHandler = missionMathHandler.copy(answer = missionMathDemoHandler.answer)
            }

            is MissionMathDemoHandler.CheckMatch -> {
                missionMathHandler =
                    if (missionMathHandler.answer == missionMathDemoHandler.subAnswer) {
                        missionMathHandler.copy(answerCorrect = true)
                    } else {
                        missionMathHandler.copy(notMatched = true)
                    }
            }

            is MissionMathDemoHandler.UpdateMatch -> {
                missionMathHandler =
                    missionMathHandler.copy(notMatched = missionMathDemoHandler.notMatched)
            }
        }
    }

    fun generateRandomIndices(totalSize: Int): List<Int> {
        val random = kotlin.random.Random.Default
        val indices = mutableListOf<Int>()
        while (indices.size < totalSize) {
            val randomIndex = random.nextInt(totalSize * totalSize)
            if (!indices.contains(randomIndex)) {
                indices.add(randomIndex)
            }
        }
        return indices
    }


    data class MissionHandlerData(
        val randomIndexList: List<Int> = emptyList(),
        val preservedIndexes: List<Int> = emptyList(),
        var correctChoiceList: List<Int> = emptyList(),
        val notMatched: Boolean = false,
        val clicked: Int = -1
    )

    data class MissionMathHandlerData(
        val answer: Int = 0,
        val notMatched: Boolean = false, val answerCorrect: Boolean = false
    )
}