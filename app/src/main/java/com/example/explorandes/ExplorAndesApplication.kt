package com.example.explorandes

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.services.LightSensorManager
import com.example.explorandes.utils.ConnectivityManager

class ExplorAndesApplication : Application() {
    lateinit var lightSensorManager: LightSensorManager
    lateinit var connectivityManager: ConnectivityManager
    lateinit var database: AppDatabase
    
    override fun onCreate() {
        super.onCreate()
        
        lightSensorManager = LightSensorManager(this) { lux ->
            Log.d("ExplorAndesApp", "Light sensor reading: $lux lux")
        }
        
        // Inicializar ConnectivityManager
        connectivityManager = ConnectivityManager(this)
        
        // Inicializar la base de datos
        database = AppDatabase.getInstance(this)
        
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val autoBrightnessEnabled = prefs.getBoolean("auto_brightness_enabled", true)
        
        Log.d("ExplorAndesApp", "Auto brightness enabled: $autoBrightnessEnabled")
    }
    
    fun saveAutoBrightnessPreference(enabled: Boolean) {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_brightness_enabled", enabled).apply()
    }
    
    fun isAutoBrightnessEnabled(): Boolean {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_brightness_enabled", true)
    }
}