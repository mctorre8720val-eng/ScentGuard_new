package com.example.scentguard.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes

class AlertAudioManager(private val context: Context) {
    private val TAG = "AlertAudioManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId: Int? = null

    /**
     * Starts playing the specified alarm sound in a loop.
     * If the same sound is already playing, it does nothing.
     */
    @Synchronized
    fun startAlarm(@RawRes resId: Int) {
        if (mediaPlayer?.isPlaying == true && currentResId == resId) {
            Log.d(TAG, "Alarm already playing: $resId")
            return
        }

        stopAlarm()

        try {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                start()
            }
            currentResId = resId
            Log.d(TAG, "Started alarm: $resId")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm: $resId", e)
        }
    }

    /**
     * Plays a one-shot preview of the sound.
     */
    @Synchronized
    fun startPreview(@RawRes resId: Int, onCompletion: () -> Unit = {}) {
        stopAlarm()

        try {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = false
                setOnCompletionListener {
                    stopAlarm()
                    onCompletion()
                }
                start()
            }
            currentResId = resId
            Log.d(TAG, "Started preview: $resId")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing preview: $resId", e)
        }
    }

    /**
     * Stops and releases the current MediaPlayer.
     */
    @Synchronized
    fun stopAlarm() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm", e)
        } finally {
            mediaPlayer = null
            currentResId = null
            Log.d(TAG, "Alarm stopped")
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
