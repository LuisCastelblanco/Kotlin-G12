package com.example.explorandes.repositories

import com.example.explorandes.api.ApiService
import com.example.explorandes.models.Event
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class EventRepository(private val apiService: ApiService) {

    companion object {
        // Singleton pattern implementation
        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(retrofit: Retrofit): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository(
                    retrofit.create(ApiService::class.java)
                ).also { instance = it }
            }
        }
    }

    fun getAllEvents(): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getAllEvents()

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to fetch events: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getEventById(id: Long): Flow<NetworkResult<Event>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getEventById(id)

            if (response.isSuccessful) {
                val event = response.body()

                if (event != null) {
                    emit(NetworkResult.Success(event))
                } else {
                    emit(NetworkResult.Error("Event not found"))
                }
            } else {
                emit(NetworkResult.Error("Failed to fetch event: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getEventsByType(type: String): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getEventsByType(type)

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to fetch events by type: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getUpcomingEvents(limit: Int = 10): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getUpcomingEvents(limit)

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to fetch upcoming events: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getEventsByLocation(locationId: Long): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getEventsByLocation(locationId)

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to fetch events by location: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun searchEvents(query: String): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.searchEvents(query)

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to search events: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getEventsByTimeRange(start: String, end: String): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getEventsByTimeRange(start, end)

            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                emit(NetworkResult.Success(events))
            } else {
                emit(NetworkResult.Error("Failed to fetch events by time range: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}