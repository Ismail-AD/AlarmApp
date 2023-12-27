package com.appdev.alarmapp.utils

import android.content.Context
import android.media.AudioManager

class VolumeControl(private val context: Context) {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // Function to set the device volume to zero
    fun setVolumeToZero() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
    }

    // Function to restore the original volume
    fun restoreOriginalVolume() {
        // You need to store the original volume values when your app starts
        // and use those values to restore the volume when needed.
        // This is just an example, you need to implement it based on your app's logic.

        val originalMusicVolume = getOriginalMusicVolume()
        val originalSystemVolume = getOriginalSystemVolume()

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0)
    }


    private fun getOriginalMusicVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    private fun getOriginalSystemVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
    }
}