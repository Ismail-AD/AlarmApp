package com.appdev.alarmapp.ui.MissionViewer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import java.lang.Integer.min
import java.math.BigDecimal
import kotlin.math.sqrt


class StepDetector : SensorEventListener {

    private var mListener: StepListener? = null
    private var steps: Int = 0


    fun setOnStepListener(listener: StepListener?) {
        mListener = listener
        Log.d("CHKST", "ASSIGNED and ${mListener != null}!")
    }

    interface StepListener {
        fun stepDetect(count: Int)
    }


    override fun onSensorChanged(event: SensorEvent) {
        Log.d("CHKST", "sensor changed ")
        Log.d("CHKST", "$mListener !")

        if (mListener != null) {
            if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                 updateStep(event.values[0],event.values[1],event.values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing for now
    }





    private val DELAY = 100
    private val MAX_VEL = 15f
    private val MIN_VEL = 6f
    private val init_vel = 1.0f

    // Threshold
    private var VEL_THRESHOLD = init_vel
    private val four_vels = floatArrayOf(init_vel, init_vel, init_vel, init_vel)
    private var pos = 0
    private var lastStepTime: Long = System.currentTimeMillis()
    private var lastVel = 0f

    // If there are no consecutive 5 steps, don't count steps
    private val WAIT_STEPS: Long = 1
    private val WAIT_MODEL = 0
    private val ACTIVITY_MODEL = 1
    private val RUN_MODEL = 2
    private var model = 0

    private var tempSteps: Long = 0
    private var initCount = 0
    private var crest = 0f
    private var trough = 0f

    private val up = 0
    private val down = 1
    private val init = 2
    private var nowStatus = init
    private var lastStatus = init

    fun updateStep(x: Float, y: Float, z: Float) {
        Log.d("StepDetector", "Current Acceleration Data: $x, $y, $z")
        val curTime = System.currentTimeMillis()
        val elapsedTime = curTime - lastStepTime
        Log.d("StepDetector", "Last Step Time: $lastStepTime")

        Log.d("StepDetector", "curr - last time: ${curTime - lastStepTime} ")
        if (elapsedTime < DELAY) return
        lastStepTime = curTime

        val b = BigDecimal(sqrt((x * x + y * y + z * z).toDouble()))
        val vel = b.setScale(2, BigDecimal.ROUND_DOWN).toFloat()
        Log.d("StepDetector", "Calculated Velocity: $vel")

        if (lastVel == 0f) lastVel = vel

        if (vel < MIN_VEL || vel > MAX_VEL) {
            Log.d("StepDetector", "Acceleration out of range (below MIN_VEL or above MAX_VEL)")
            initStepDetector()
            return
        }

        VEL_THRESHOLD = getVEL_THRESHOLD()
        Log.d("StepDetector", "Current Velocity Threshold: $VEL_THRESHOLD")

        if (vel > lastVel) {
            if (lastStatus == up || crest == 0f) crest = vel
            if (trough == 0f) trough = lastVel
            nowStatus = up
        } else if (vel <= lastVel) {
            if (lastStatus == down || trough == 0f) trough = vel
            if (crest == 0f) crest = lastVel
            nowStatus = down
        }

        Log.d("StepDetector", "Velocity: $vel, Last Velocity: $lastVel")
        Log.d("StepDetector", "Status: $nowStatus, Last Status: $lastStatus")
        Log.d("StepDetector", "Wave Crest: $crest, Wave Trough: $trough")

        if (nowStatus != lastStatus && nowStatus != init && lastStatus != init) {
            Log.d("StepDetector", "Wave Change: ${crest - trough}")

            if (crest - trough >= VEL_THRESHOLD) {
                realSteps(1)
                updateVEL_THRESHOLD(crest - trough)
            } else {
                Log.d("StepDetector", "Velocity Change Below Threshold. Resetting Step Detector.")
                initStepDetector()
            }

            trough = 0f
            crest = trough
        }

        lastVel = vel
        lastStatus = nowStatus
    }

    private fun realSteps(num: Long) {
        Log.d("StepDetector", "-----STEP DETECTED----")
        Log.d("StepDetector", "Current Model: $model")
        initCount = 0

        if (model == ACTIVITY_MODEL || model == RUN_MODEL) {
            Log.d("StepDetector", "----In IF of MODELS")
            mListener?.stepDetect((steps + num).toInt())
        } else {
            Log.d("StepDetector", "Temporary Steps: $tempSteps")
            Log.d("StepDetector", "----TEMP : $tempSteps ")
            if (tempSteps >= WAIT_STEPS) {
                model = RUN_MODEL
                mListener?.stepDetect((num + tempSteps).toInt())
                tempSteps = 0
            } else {
                Log.d("StepDetector", "----TEMP INCREASED")
                tempSteps++
            }
        }
    }

    private fun initThreshold() {
        pos = 0

        for (i in 0 until 4) {
            four_vels[i] = init_vel
        }
    }

    private fun initStepDetector() {
        Log.d("StepDetector", "Initialization Count: $initCount")

        if (initCount < 2) {
            initCount++
            return
        }

        Log.d("StepDetector", "Initializing Step Detector")
        nowStatus = init
        lastStatus = init
        crest = 0f
        trough = 0f
        initThreshold()

        if (model == RUN_MODEL) {
            model = WAIT_MODEL
        }

        tempSteps = 0
        initCount = 0
    }

    private fun updateVEL_THRESHOLD(vel: Float) {
        four_vels[pos++] = vel

        if (pos == 4) {
            pos = 0
        }

        Log.d("StepDetector", "Recent Velocities: ${four_vels[0]}, ${four_vels[1]}, ${four_vels[2]}, ${four_vels[3]}")
    }

    private fun getVEL_THRESHOLD(): Float {
        var sum = 0f

        for (i in 0 until 4) {
            sum += four_vels[i]
        }

        sum /= 4
        return sum
    }

}
