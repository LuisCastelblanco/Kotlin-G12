package com.example.explorandes.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * Clase para ayudar a verificar el estado de conectividad de la app
 */
class ConnectivityHelper(private val context: Context) {
    
    private val TAG = "ConnectivityHelper"
    
    
    /**
     * Verifica si hay una conexión a Internet disponible
     */
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        Log.d(TAG, "Internet availability check: $hasInternet")
        return hasInternet
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ConnectivityHelper? = null
        
        fun getInstance(context: Context): ConnectivityHelper {
            return INSTANCE ?: synchronized(this) {
                ConnectivityHelper(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}