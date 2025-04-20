package com.example.explorandes.api

import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.AuthResponse
import com.example.explorandes.models.RegisterRequest
import com.example.explorandes.models.User
import com.example.explorandes.models.Building
import com.example.explorandes.models.Place
import com.example.explorandes.models.Event
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

    // Building and place endpoints with correct paths
    @GET("api/buildings")
    suspend fun getAllBuildings(): Response<List<Building>>

    @GET("api/buildings/{id}")
    suspend fun getBuildingById(@Path("id") id: Long): Response<Building>

    @GET("api/buildings/category/{category}")
    suspend fun getBuildingsByCategory(@Path("category") category: String): Response<List<Building>>

    @GET("api/buildings/nearby")
    suspend fun getNearbyBuildings(
        @Query("userLat") userLat: Double,
        @Query("userLon") userLon: Double
    ): Response<List<Building>>

    @GET("api/places")
    suspend fun getAllPlaces(): Response<List<Place>>

    @GET("api/places/{id}")
    suspend fun getPlaceById(@Path("id") id: Long): Response<Place>

    @GET("api/places/building/{buildingId}")
    suspend fun getPlacesByBuilding(@Path("buildingId") buildingId: Long): Response<List<Place>>

    @GET("api/places/category/{category}")
    suspend fun getPlacesByCategory(@Path("category") category: String): Response<List<Place>>

    @GET("api/places/search")
    suspend fun searchPlaces(@Query("query") query: String): Response<List<Place>>

    @GET("api/events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @GET("api/events/type/{type}")
    suspend fun getEventsByType(@Path("type") type: String): Response<List<Event>>

    @GET("api/events/location/{locationId}")
    suspend fun getEventsByLocation(@Path("locationId") locationId: Long): Response<List<Event>>

    @GET("api/events/upcoming")
    suspend fun getUpcomingEvents(@Query("limit") limit: Int = 10): Response<List<Event>>

    @GET("api/events/search")
    suspend fun searchEvents(@Query("query") query: String): Response<List<Event>>

    @GET("api/events/timeRange")
    suspend fun getEventsByTimeRange(
        @Query("start") start: String,
        @Query("end") end: String
    ): Response<List<Event>>
}