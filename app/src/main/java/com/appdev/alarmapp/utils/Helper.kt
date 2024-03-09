package com.appdev.alarmapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.core.net.toUri
import java.io.File

class Helper {
    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var currentPosition = 0
        private var volumeHandler: Handler? = null
        private var currentVolume = 20
        private var customVolume = 100f
        var lowIt = false

        fun isPlaying(): Boolean {
            return mediaPlayer?.isPlaying ?: false
        }

        fun updateLow(value: Boolean) {
            lowIt = value
        }

        fun updateCustomValue(value: Float) {
            customVolume = value
        }

        fun playFile(file: File, context: Context) {
            try {
                if (file != null) {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer.create(context, file.toUri())
                    mediaPlayer?.seekTo(currentPosition)
                    if (lowIt) {
                        mediaPlayer?.setVolume(currentVolume / 100f, currentVolume / 100f)
                    }
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener {
                        mediaPlayer?.start()
                    }
                }

            } catch (e: Exception) {
                Log.d("CHECCHKALM", "${e.localizedMessage} AND AND" + file.toUri().toString())
            }
        }

        fun startIncreasingVolume(l: Long = 2000L) {
            Log.d("CHECCHKALM", "SET")
            volumeHandler = Handler()
            volumeHandler?.postDelayed(object : Runnable {
                override fun run() {
                    if(isPlaying()){
                        if (currentVolume <= customVolume) {
                            mediaPlayer?.setVolume(currentVolume / 100f, currentVolume / 100f)
                            currentVolume += 20
                            volumeHandler?.postDelayed(this, l)
                        } else {
                            // Continue playing at maximum volume
                            volumeHandler?.postDelayed(this, l)
                        }
                        Log.d("CHKMUS","--UPDATED CURRENT VOLUME ${currentVolume}")
                    }
                }
            }, l)
        }

        fun stopIncreasingVolume() {
            volumeHandler?.removeCallbacksAndMessages(null)
            currentVolume = 20
            mediaPlayer?.setVolume(currentVolume / 100f, currentVolume / 100f)
        }


        fun playStream(context: Context, rawId: Int = -1, uri: Uri? = null) {
            if (rawId != -1) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, rawId)
                mediaPlayer?.seekTo(currentPosition)
                if (lowIt) {
                    mediaPlayer?.setVolume(currentVolume / 100f, currentVolume / 100f)
                }
                mediaPlayer?.start()
                Log.d("CHKMUS","GOING TO PLAY ${isPlaying()}")
                mediaPlayer?.setOnCompletionListener {
                    // Handle audio completion for looping
                    mediaPlayer?.start()
                }
            }
            uri?.let {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, it)
                mediaPlayer?.seekTo(currentPosition)
                if (lowIt) {
                    mediaPlayer?.setVolume(currentVolume / 100f, currentVolume / 100f)
                }
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    // Handle audio completion for looping
                    mediaPlayer?.start()
                }
            }
        }

        fun pauseStream() {
            mediaPlayer?.let {
                currentPosition = it.currentPosition
                it.pause()
            }
        }

        fun stopStream() {
            Log.d("CHKMUS", "STOPPED MUSIC VIA METHOD")
            stopIncreasingVolume()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            currentPosition = 0
        }

        fun releasePlayer() {
            Log.d("CHKMUS", "PLAYER RELEASED")
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPosition = 0
        }
    }
}