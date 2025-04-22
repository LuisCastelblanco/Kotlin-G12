package com.example.explorandes.services

import android.app.Activity
import android.provider.Settings
import android.view.WindowManager

object BrightnessController {
    fun setBrightness(activity: Activity, brightness: Float) {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = brightness.coerceIn(0.1f, 1.0f) // Ensure valid range
        activity.window.attributes = layoutParams
    }
}
