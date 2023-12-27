package com.appdev.alarmapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File

class Helper {
    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var currentPosition = 0

        fun isPlaying(): Boolean {
            return mediaPlayer?.isPlaying ?: false
        }

        fun playFile(file: File, context: Context) {
            try {
                if (file != null) {
                    Log.d("CHKALM","FILE IS NOT NULL  "+ file.toUri().toString())
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer.create(context, file.toUri())
                    mediaPlayer?.seekTo(currentPosition)
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener {
                        // Handle audio completion for looping
                        Log.d("CHECCHKALM"," URI PLAYING IS "+ file.toUri().toString())
                        mediaPlayer?.start()
                    }
                }

            } catch (e: Exception) {
                Log.d("CHECCHKALM","${e.localizedMessage} AND AND"+ file.toUri().toString())
            }
        }

        fun playStream(context: Context, rawId: Int = -1, uri: Uri? = null) {
            if (rawId != -1) {
                Log.d("CHKALM", "RAW PLAYED AUDIO CALLED")
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, rawId)
                mediaPlayer?.seekTo(currentPosition)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    // Handle audio completion for looping
                    mediaPlayer?.start()
                }
            }
            uri?.let {
                Log.d("CHKALM", "URI PLAY CALLED")
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, it)
                mediaPlayer?.seekTo(currentPosition)
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
            Log.d("CHKALM", "STOPPED MUSIC VIA METHOD")
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            currentPosition = 0
        }

        fun releasePlayer() {
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPosition = 0
        }
    }
}