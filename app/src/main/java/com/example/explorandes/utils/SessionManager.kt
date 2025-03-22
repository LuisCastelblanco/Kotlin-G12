package com.example.explorandes.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Session manager to save and fetch authentication token
 */
class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "explorandes_prefs"
        private const val AUTH_TOKEN = "auth_token"
    }

    /**
     * Save authentication token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    /**
     * Fetch authentication token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    /**
     * Clear authentication token
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }
}