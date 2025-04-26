package com.example.explorandes.repositories

import android.util.Log
import com.example.explorandes.api.ApiClient
import com.example.explorandes.database.dao.BuildingDao
import com.example.explorandes.database.entity.BuildingEntity
import com.example.explorandes.models.Building
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class BuildingRepository(private val buildingDao: BuildingDao) {

    suspend fun getAllBuildings(): Flow<NetworkResult<List<Building>>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            // Primero emitir datos de la caché local
            val localBuildings = buildingDao.getAllBuildings().map { entities ->
                entities.map { it.toModel() }
            }
            
            // Intentar cargar datos remotos
            val response = ApiClient.apiService.getAllBuildings()
            if (response.isSuccessful) {
                val buildings = response.body()
                if (buildings != null) {
                    // Guardar en la base de datos local
                    val buildingEntities = buildings.map { BuildingEntity.fromModel(it) }
                    buildingDao.insertBuildings(buildingEntities)
                    emit(NetworkResult.Success(buildings))
                } else {
                    emit(NetworkResult.Error("Response body was null"))
                }
            } else {
                // En caso de error, usar datos locales
                emit(NetworkResult.Error("Error: ${response.code()} - ${response.message()}", null))
            }
        } catch (e: IOException) {
            // Error de red, usar datos locales
            Log.e("BuildingRepository", "Network error", e)
            emit(NetworkResult.Error("Network error: ${e.localizedMessage}", null))
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error fetching buildings", e)
            emit(NetworkResult.Error("Error: ${e.localizedMessage}", null))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBuildingById(id: Long): Flow<NetworkResult<Building>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            // Primero intentar desde local
            val localBuilding = buildingDao.getBuildingById(id)
            if (localBuilding != null) {
                emit(NetworkResult.Success(localBuilding.toModel()))
            }
            
            // Intentar remoto
            val response = ApiClient.apiService.getBuildingById(id)
            if (response.isSuccessful) {
                val building = response.body()
                if (building != null) {
                    // Guardar en local
                    buildingDao.insertBuilding(BuildingEntity.fromModel(building))
                    emit(NetworkResult.Success(building))
                } else {
                    if (localBuilding == null) {
                        emit(NetworkResult.Error("Building not found"))
                    }
                }
            } else {
                if (localBuilding == null) {
                    emit(NetworkResult.Error("Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: IOException) {
            // Error de red, usar datos locales si están disponibles
            if (localBuilding == null) {
                emit(NetworkResult.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: Exception) {
            if (localBuilding == null) {
                emit(NetworkResult.Error("Error: ${e.localizedMessage}"))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBuildingsByCategory(category: String): Flow<NetworkResult<List<Building>>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            // Primero emitir datos locales
            val localBuildings = buildingDao.getBuildingsByCategory(category).map { entities ->
                entities.map { it.toModel() }
            }
            
            // Intentar cargar remotos
            val response = ApiClient.apiService.getBuildingsByCategory(category)
            if (response.isSuccessful) {
                val buildings = response.body()
                if (buildings != null) {
                    // Guardar en local
                    val buildingEntities = buildings.map { BuildingEntity.fromModel(it) }
                    buildingDao.insertBuildings(buildingEntities)
                    emit(NetworkResult.Success(buildings))
                } else {
                    emit(NetworkResult.Error("Response body was null"))
                }
            } else {
                emit(NetworkResult.Error("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            // Error de red, usar datos locales
            Log.e("BuildingRepository", "Network error", e)
            emit(NetworkResult.Error("Network error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error fetching buildings", e)
            emit(NetworkResult.Error("Error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchBuildings(query: String): Flow<NetworkResult<List<Building>>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            // Buscar en local
            val localBuildings = buildingDao.searchBuildings(query).map { entities ->
                entities.map { it.toModel() }
            }
            
             emit(NetworkResult.Success(localBuildings.first()))
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error searching buildings", e)
            emit(NetworkResult.Error("Error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}