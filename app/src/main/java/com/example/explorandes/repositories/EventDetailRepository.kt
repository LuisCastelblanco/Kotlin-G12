package com.example.explorandes.repositories

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.explorandes.api.ApiClient
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.database.entity.EventDetailEntity
import com.example.explorandes.models.EventDetail
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class EventDetailRepository(private val context: Context) {
    
    private val db = AppDatabase.getInstance(context)
    private val eventDetailDao = db.eventDetailDao()
    
    // Verificar si hay conexión a internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    // Obtener detalle del evento con manejo de caché
    fun getEventDetail(eventId: Long): Flow<NetworkResult<EventDetail>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            // Primero intentar cargar desde la caché
            val cachedDetail = eventDetailDao.getEventDetailById(eventId)
            
            // Si hay datos en caché, emitirlos primero
            if (cachedDetail != null) {
                emit(NetworkResult.Success(cachedDetail.toModel()))
            }
            
            // Si hay conexión a internet, intentar actualizar los datos
            if (isNetworkAvailable()) {
                try {
                    val response = ApiClient.apiService.getEventById(eventId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val eventFromNetwork = response.body()!!
                        
                        // Convertir Event básico a EventDetail y añadir detalles adicionales
                        val eventDetail = EventDetail(
                            id = eventFromNetwork.id,
                            title = eventFromNetwork.title,
                            description = eventFromNetwork.description,
                            imageUrl = eventFromNetwork.imageUrl,
                            type = eventFromNetwork.type,
                            startTime = eventFromNetwork.startTime,
                            endTime = eventFromNetwork.endTime,
                            locationId = eventFromNetwork.locationId,
                            locationName = eventFromNetwork.locationName,
                            // Aquí podrías agregar más detalles específicos que vengan del API
                            organizerName = null, // Suponiendo que estos datos no vienen en la respuesta básica
                            capacity = null,
                            registrationUrl = null,
                            additionalInfo = null
                        )
                        
                        // Guardar en caché
                        eventDetailDao.insertEventDetail(EventDetailEntity.fromModel(eventDetail))
                        
                        // Emitir los nuevos datos
                        emit(NetworkResult.Success(eventDetail))
                    } else {
                        // Si hay error en la API pero tenemos caché, no emitimos error
                        if (cachedDetail == null) {
                            emit(NetworkResult.Error("Error cargando el evento: ${response.code()}"))
                        }
                    }
                } catch (e: Exception) {
                    // Si hay error de red pero tenemos caché, no emitimos error
                    if (cachedDetail == null) {
                        emit(NetworkResult.Error("Error de red: ${e.localizedMessage}"))
                    }
                }
            } else {
                // Si no hay internet y no hay caché, emitir error
                if (cachedDetail == null) {
                    emit(NetworkResult.Error("No hay conexión a Internet y no hay datos en caché"))
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error al acceder a la base de datos: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Observar un evento específico desde la base de datos
    fun observeEventDetail(eventId: Long): Flow<EventDetail?> {
        return eventDetailDao.observeEventDetailById(eventId)
            .map { entity -> entity?.toModel() }
            .flowOn(Dispatchers.IO)
    }
    
    // Guardar cambios locales y marcarlos para sincronización
    suspend fun saveEventDetail(eventDetail: EventDetail) {
        withContext(Dispatchers.IO) {
            val entity = EventDetailEntity.fromModel(eventDetail).copy(
                lastSyncStatus = if (isNetworkAvailable()) "synced" else "pending_sync"
            )
            eventDetailDao.insertEventDetail(entity)
            
            // Si hay conexión, sincronizar inmediatamente
            if (isNetworkAvailable()) {
                syncEventDetail(entity)
            }
        }
    }
    
    // Sincronizar eventos pendientes
    suspend fun syncPendingEvents() {
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable()) return@withContext
            
            val pendingEvents = eventDetailDao.getPendingSyncEventDetails()
            for (event in pendingEvents) {
                syncEventDetail(event)
            }
        }
    }
    
    // Sincronizar un evento específico con el servidor
    private suspend fun syncEventDetail(eventDetailEntity: EventDetailEntity) {
        if (!isNetworkAvailable()) return
        
        try {
   
            eventDetailDao.updateSyncStatus(eventDetailEntity.id, "synced")
            
            Log.d("EventDetailRepository", "Evento sincronizado: ${eventDetailEntity.id}")
        } catch (e: Exception) {
            Log.e("EventDetailRepository", "Error sincronizando evento: ${e.localizedMessage}")
            eventDetailDao.updateSyncStatus(eventDetailEntity.id, "sync_error")
        }
    }
}