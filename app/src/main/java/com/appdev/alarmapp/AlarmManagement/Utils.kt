package com.appdev.alarmapp.AlarmManagement

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.appdev.alarmapp.ModelClass.SnoozeTimer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class Utils(private val context: Context) {

    private val PREFS_NAME = "snooze_timers"
    private val SNOOZE_TIMERS_KEY = "snooze_timers_set"
    private val SHARED_PREFS_NAME = "MySharedPrefs"
    private val VOLUME_KEY = "volume"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveVolume(volume: Int) {
        sharedPreferences.edit().putInt(VOLUME_KEY, volume).apply()
    }

    fun getCurrentVolume(): Int {
        return sharedPreferences.getInt(VOLUME_KEY,100)
    }
    fun removeVolume() {
        sharedPreferences.edit().remove(VOLUME_KEY).apply()
    }
    fun isVolumeEmpty(): Boolean {
        return !sharedPreferences.contains(VOLUME_KEY)
    }

    fun startOrUpdateSnoozeTimer(snoozeTimer: SnoozeTimer) {
        val timers = mutableSetOf<SnoozeTimer>()
        timers.addAll(getSnoozeTimers())
        timers.add(snoozeTimer)
        saveSnoozeTimers(timers)
    }

    fun getSnoozeTimerById(alarmId: Long): SnoozeTimer? {
        val timers = getSnoozeTimers()
        return timers.find { it.alarmId == alarmId }
    }

    fun stopSnoozeTimer(alarmId: Long) {
        val timers = getSnoozeTimers().toMutableSet() // Convert to mutable set
        val removed = timers.removeIf { it.alarmId == alarmId } // Remove the timer

        if (removed) { // Check if any timer was removed
            saveSnoozeTimers(timers) // Save the updated list only if a timer was removed
            Log.d("CHKSM", "Alarm with ID $alarmId removed from snooze timers")
        } else {
            Log.d("CHKSM", "Alarm with ID $alarmId not found in snooze timers")
        }
    }

    private fun saveSnoozeTimers(timers: Set<SnoozeTimer>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonTimers = Gson().toJson(timers)
        val editor = prefs.edit()
        editor.putString(SNOOZE_TIMERS_KEY, jsonTimers)
        editor.apply()
        Log.d(
            "CHKSM",
            "****** NEW LIST SAVED"
        )
    }

    fun getSnoozeTimers(): Set<SnoozeTimer> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonTimers = prefs.getString(SNOOZE_TIMERS_KEY, null)
        return if (jsonTimers != null) {
            Gson().fromJson(jsonTimers, object : TypeToken<Set<SnoozeTimer>>() {}.type)
        } else {
            emptySet()
        }
    }

    fun findClosestSnoozeTimer(): SnoozeTimer? {
        val timers = getSnoozeTimers()
        Log.d("CHJ","CALLED TO GET SHORT")
        return timers.minByOrNull { it.remainingTimeMillis }
    }

    fun areSnoozeTimersEmpty(): Boolean {
        return getSnoozeTimers().isEmpty()
    }
}
