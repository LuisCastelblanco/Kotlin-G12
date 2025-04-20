package com.example.explorandes.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

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
        Log.d("SessionManager", "Token guardado: $token")
    }

    // Obtener token JWT
    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getUsername(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    // Guardar datos básicos del usuario
    fun saveUserInfo(id: Long, email: String, name: String) {
        Log.d("SessionManager", "Guardando info de usuario: ID=$id, Email=$email, Name=$name")
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

    fun isLoggedIn(): Boolean {
        val token = getToken()
        val userId = getUserId()
        val hasToken = !token.isNullOrEmpty()
        val hasUserId = userId > 0

        Log.d("SessionManager", "isLoggedIn check: Token=${hasToken}, UserId=${hasUserId}")

        // Require both token and userId to be valid
        return hasToken && hasUserId
    }

    fun logout() {
        Log.d("SessionManager", "Cerrando sesión y eliminando datos")
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}