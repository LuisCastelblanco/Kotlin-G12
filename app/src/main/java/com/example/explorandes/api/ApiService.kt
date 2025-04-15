package com.example.explorandes.api

import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.AuthResponse
import com.example.explorandes.models.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): AuthResponse
    
    @POST("auth/register")
    suspend fun register(@Body user: User): AuthResponse
}