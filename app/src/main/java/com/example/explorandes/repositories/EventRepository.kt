package com.example.explorandes.repositories

import android.content.Context
import com.example.explorandes.api.ApiService
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.database.entity.EventEntity
import com.example.explorandes.models.Event
import com.example.explorandes.utils.ConnectivityHelper
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.IOException

class EventRepository(private val apiService: ApiService, private val context: Context? = null) {

    // Local database DAO
    private val eventDao = context?.let { AppDatabase.getInstance(it).eventDao() }
    private val connectivityHelper = context?.let { ConnectivityHelper(it) }

    fun isInternetAvailable(): Boolean {
        return connectivityHelper?.isInternetAvailable() ?: false
    }

    companion object {
        // Singleton pattern implementation
        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(retrofit: Retrofit, context: Context): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository(
                    retrofit.create(ApiService::class.java),
                    context
                ).also { instance = it }
            }
        }
        
        // Add a new getInstance method that accepts ApiService directly
        fun getInstance(apiService: ApiService, context: Context): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository(apiService, context).also { instance = it }
            }
        }
    }

    fun getAllEvents(): Flow<NetworkResult<List<Event>>> = flow {
        emit(NetworkResult.Loading())
    
        try {
            // Try API first if internet is available
            if (connectivityHelper?.isInternetAvailable() == true) {
                try {
                    val response = apiService.getAllEvents()
    
                    if (response.isSuccessful) {
                        val events = response.body() ?: emptyList()
                        
                        // Save to cache if database is available
                        eventDao?.let { dao ->
                            withContext(Dispatchers.IO) {
                                val eventEntities = events.map { EventEntity.fromModel(it) }
                                dao.insertEvents(eventEntities)
                            }
                        }
                        
                        emit(NetworkResult.Success(events))
                    } else {
                        // API error - try cache
                        loadFromCache(
                            emit = { result -> emit(result) },
                            errorMessage = "API error: ${response.code()}"
                        )
                    }
                } catch (e: Exception) {
                    // Network error - try cache
                    loadFromCache(
                        emit = { result -> emit(result) },
                        errorMessage = "Network error: ${e.message}"
                    )
                }
            } else {
                // No internet - try cache
                loadFromCache(
                    emit = { result -> emit(result) },
                    errorMessage = "No internet connection"
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error fetching events: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    private suspend fun loadFromCache(
    emit: suspend (NetworkResult<List<Event>>) -> Unit,
    errorMessage: String? = null
    ) {
        // Only try cache if we have a database
        if (eventDao != null) {
            try {
                val cachedEvents = eventDao.getAllEvents().first()
                val events = cachedEvents.map { it.toModel() }
                
                if (events.isNotEmpty()) {
                    emit(NetworkResult.Success(events))
                } else {
                    emit(NetworkResult.Error(errorMessage ?: "No cached events available", emptyList()))
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error("Failed to load cached events: ${e.message}", emptyList()))
            }
        } else {
            emit(NetworkResult.Error(errorMessage ?: "Cache not available", emptyList()))
        }
    }

    fun getEventById(id: Long): Flow<NetworkResult<Event>> = flow {
        emit(NetworkResult.Loading())
    
        try {
            // Try API first if internet is available
            if (connectivityHelper?.isInternetAvailable() == true) {
                try {
                    val response = apiService.getEventById(id)
    
                    if (response.isSuccessful) {
                        val event = response.body()
    
                        if (event != null) {
                            // Save to cache if database is available
                            eventDao?.let { dao ->
                                withContext(Dispatchers.IO) {
                                    dao.insertEvent(EventEntity.fromModel(event))
                                }
                            }
                            
                            emit(NetworkResult.Success(event))
                        } else {
                            // Not found in API - try cache
                            loadEventByIdFromCache(
                                id,
                                emit = { result -> emit(result) }
                            )
                        }
                    } else {
                        // API error - try cache
                        loadEventByIdFromCache(
                            id,
                            emit = { result -> emit(result) },
                            errorMessage = "API error: ${response.code()}"
                        )
                    }
                } catch (e: Exception) {
                    // Network error - try cache
                    loadEventByIdFromCache(
                        id,
                        emit = { result -> emit(result) },
                        errorMessage = "Network error: ${e.message}"
                    )
                }
            } else {
                // No internet - try cache
                loadEventByIdFromCache(
                    id,
                    emit = { result -> emit(result) },
                    errorMessage = "No internet connection"
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error fetching event: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    private suspend fun loadEventByIdFromCache(
    id: Long,
    emit: suspend (NetworkResult<Event>) -> Unit,
    errorMessage: String? = null
    ) {
        // Only try cache if we have a database
        if (eventDao != null) {
            try {
                val cachedEvent = eventDao.getEventById(id)
                
                if (cachedEvent != null) {
                    emit(NetworkResult.Success(cachedEvent.toModel()))
                } else {
                    emit(NetworkResult.Error(errorMessage ?: "Event not found in cache"))
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error("Failed to load cached event: ${e.message}"))
            }
        } else {
            emit(NetworkResult.Error(errorMessage ?: "Cache not available"))
        }
    }


}