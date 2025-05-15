package com.example.explorandes.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class LightSensorManager(
    context: Context, 
    private var onLightChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    private var lastReading = -1f
    private val alpha = 0.2f  
    

    fun registerCallback(callback: (Float) -> Unit) {
        onLightChanged = callback
    }
    
    fun startListening() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("LightSensorManager", "Started listening to light sensor")
        } ?: run {
            Log.e("LightSensorManager", "No light sensor found on this device")
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        Log.d("LightSensorManager", "Stopped listening to light sensor")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                // Apply low-pass filter to smooth readings
                val rawLux = it.values[0]
                val lux = if (lastReading < 0) {
                    rawLux
                } else {
                    lastReading + alpha * (rawLux - lastReading)
                }
                lastReading = lux
                
                //Log.d("LightSensorManager", "Ambient light level: $lux lux (raw: $rawLux)")
                onLightChanged(lux)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_LIGHT) {
           // Log.d("LightSensorManager", "Light sensor accuracy changed: $accuracy")
        }
    }
}