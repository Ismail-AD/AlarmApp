package com.appdev.alarmapp.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AudioRecorder {
    companion object{
        private var recorder: MediaRecorder? = null

        private fun createRecorder(context: Context): MediaRecorder {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else MediaRecorder()
        }

        fun startRecording(outputFile: File,context: Context) {
            createRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(outputFile).fd)

                prepare()
                start()

                recorder = this
            }
        }

        fun stopRecording() {
            recorder?.stop()
            recorder?.reset()
            recorder = null
        }
    }

}