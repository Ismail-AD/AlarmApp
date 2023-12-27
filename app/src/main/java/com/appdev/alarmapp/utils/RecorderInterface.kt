package com.appdev.alarmapp.utils

import java.io.File

interface RecorderInterface {
    fun startRecording(outputFile: File)
    fun stopRecording()
}