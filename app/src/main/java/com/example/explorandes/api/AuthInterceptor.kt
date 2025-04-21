package com.example.explorandes.api

import com.example.explorandes.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Si no hay token, seguimos sin modificar la solicitud
        val token = sessionManager.getToken() ?: return chain.proceed(originalRequest)
        
        // Si hay token, lo añadimos al encabezado de la solicitud
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
}