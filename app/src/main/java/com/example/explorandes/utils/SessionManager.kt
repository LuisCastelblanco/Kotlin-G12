package com.example.explorandes.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        const val PREF_NAME = "ExplorandesPrefs"
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
    }
    
    // Guardar token JWT
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }
    
    // Obtener token JWT
    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    
    // Guardar datos básicos del usuario
    fun saveUserInfo(id: Long, email: String, name: String) {
        val editor = prefs.edit()
        editor.putLong(USER_ID, id)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_NAME, name)
        editor.apply()
    }
    
    // Obtener ID del usuario
    fun getUserId(): Long {
        return prefs.getLong(USER_ID, -1)
    }
    
    // Verificar si el usuario está logueado
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    // Cerrar sesión - eliminar todos los datos guardados
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}