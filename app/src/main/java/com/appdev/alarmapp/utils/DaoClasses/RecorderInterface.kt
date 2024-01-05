package com.appdev.alarmapp.utils.DaoClasses

import java.io.File

interface RecorderInterface {
    fun startRecording(outputFile: File)
    fun stopRecording()
}