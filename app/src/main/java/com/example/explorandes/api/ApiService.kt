package com.example.explorandes.api

import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.AuthResponse
import com.example.explorandes.models.RegisterRequest
import com.example.explorandes.models.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: User): Response<User>

    @Multipart
    @POST("api/users/{userId}/profile-image")
    suspend fun uploadProfileImage(
        @Path("userId") userId: Long,
        @Part image: MultipartBody.Part
    ): Response<User>
}