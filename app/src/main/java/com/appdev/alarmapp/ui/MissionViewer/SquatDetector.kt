package com.appdev.alarmapp.ui.MissionViewer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class SquatDetector : SensorEventListener {
    interface OnSquatListener {
        fun onSquat()
    }

    fun setOnSquatListener(listener: OnSquatListener) {
        squatListener = listener
    }

    private var squatListener: OnSquatListener? = null
    private var squatStartTime: Long = 0
    private var squatEndTime: Long = 0
    private var squatInProgress: Boolean = false
    private var motionCheckInProgress: Boolean = false
    private var upwardMotionCounter = 0

    //D--z(5.5,7.0)  g(2.9,5.0)
    //U--z(7.2,8.5)  g(1.5,2.5)

    //D--z(9.9,11.9)  g(0.3,2.1)

    //U--z(6.9,8.0)  g(2.7,3.0)
    //U--z(6.3,7.0)  g(2.8,4.0)
    //U--z(6.3,7.0)  g(2.8,4.0)
    //U-----0.75 ---- 2.5     z(9.1,7.4)

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {


            //DOWN Gravity increases , acceleration decreases ()
//             Check for stability during the up phase
            if (!squatInProgress && !motionCheckInProgress) {
                Log.d(
                    "CHKMV",
                    "New Squat time Delay : ${System.currentTimeMillis() - squatEndTime} "
                )
                //System.currentTimeMillis() - squatStartTime > 500L
                if (System.currentTimeMillis() - squatEndTime > 800L) {
                    squatStartTime = System.currentTimeMillis()
                    Log.d("CHKMV", "Y DOWN:--- ${event.values[1]}")
                    startMotionCheck()
                    checkForDownwardMotion(event.values[1])
                    endMotionCheck()
                }
            }
//            // Check for stability during the down phase
            if (squatInProgress && !motionCheckInProgress) {
                Log.d("CHKMV", "Y UP:---  ${event.values[1]} ")
                startMotionCheck()
                checkForUpwardMotion(event.values[1])
                endMotionCheck()
            }
        }

    }

    private fun startMotionCheck() {
        motionCheckInProgress = true
    }

    private fun endMotionCheck() {
        motionCheckInProgress = false
    }


    private fun checkForDownwardMotion(accelerationZ: Float) {
        if (accelerationZ < 9.2 && accelerationZ > 8.0) {
            Log.d("CHKMV", "Successful DOWN: $accelerationZ ")
            // User is going down
//            Log.d("CHKMV", "Time of Start: $squatStartTime ")
            squatInProgress = true
        }
    }

    private fun checkForUpwardMotion(accelerationZ: Float) {
        // User is going up
        upwardMotionCounter++
        if (accelerationZ < 4.0 && accelerationZ > 2.9) {
            squatEndTime = System.currentTimeMillis()
            Log.d("CHKMV", "Successful UP: $accelerationZ and counter is $upwardMotionCounter")
            // Squat completed within 5 seconds
            squatInProgress = false
            squatListener?.onSquat()
        }
        if (upwardMotionCounter > 24) {
            squatInProgress = false
            upwardMotionCounter = 0
        }
    }

    private fun isStablePosition(
        event: SensorEvent,
        gravityThresholdStart: Double,
        gravityThresholdEnd: Double
    ): Boolean {
        val value = abs(event.values[2] - 9.8)
        // Implement your logic to check if the device is in a stable position
        // You might want to use other sensor values or a combination of them for this check
        // For simplicity, this example assumes that the device is stable when the z-axis acceleration is close to 9.8 m/s^2 (gravity)
        Log.d("CHKMV", "Gravity : $value")
        return value > gravityThresholdStart && value < gravityThresholdEnd
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle changes in sensor accuracy if needed
    }
}