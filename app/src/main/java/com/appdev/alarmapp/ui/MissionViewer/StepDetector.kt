package com.appdev.alarmapp.ui.MissionViewer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class StepDetector : SensorEventListener {
    private var mListener: StepListener? = null
    var steps: Int = 0
    fun setOnStepListener(listener: StepListener?) {
        mListener = listener
    }

    interface StepListener {
        fun stepDetect(count: Int)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mListener != null) {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                Log.d("STEPD", "STEP DETKT")
                mListener!!.stepDetect(steps++)
                Log.d("STEPD", "STEP value is $steps")
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {

    }


}