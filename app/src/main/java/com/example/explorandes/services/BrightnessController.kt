package com.example.explorandes.services

import android.app.Activity
import android.util.Log
import android.view.WindowManager

object BrightnessController {
    private var originalBrightness = -1f
    
    fun setBrightness(activity: Activity, brightness: Float) {
        try {
            if (originalBrightness < 0) {
                originalBrightness = activity.window.attributes.screenBrightness
                if (originalBrightness < 0) originalBrightness = 0.5f 
               // Log.d("BrightnessController", "Original brightness saved: $originalBrightness")
            }
            
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = brightness.coerceIn(0.1f, 1.0f)
            activity.window.attributes = layoutParams
           // Log.d("BrightnessController", "Brightness set to: ${layoutParams.screenBrightness}")
        } catch (e: Exception) {
            Log.e("BrightnessController", "Error setting brightness", e)
        }
    }
    
    fun resetBrightness(activity: Activity) {
        if (originalBrightness >= 0) {
            try {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = originalBrightness
                activity.window.attributes = layoutParams
                Log.d("BrightnessController", "Reset brightness to original: $originalBrightness")
            } catch (e: Exception) {
                Log.e("BrightnessController", "Error resetting brightness", e) 
            }
        }
    }
}