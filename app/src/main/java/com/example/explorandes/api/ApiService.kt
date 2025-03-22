package com.example.explorandes.api

import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): AuthResponse
}