package com.example.explorandes.repositories

import android.content.Context
import android.util.Log
import com.example.explorandes.api.ApiClient
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.database.entity.BuildingEntity
import com.example.explorandes.models.Building
import com.example.explorandes.utils.ConnectivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BuildingRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val buildingDao = db.buildingDao()
    private val connectivityHelper = ConnectivityHelper(context)

    fun isInternetAvailable(): Boolean {
        return connectivityHelper.isInternetAvailable()
    }

    suspend fun getAllBuildings(): List<Building> = withContext(Dispatchers.IO) {
        // Primero verificamos si hay internet
        if (connectivityHelper.isInternetAvailable()) {
            try {
                Log.d("BuildingRepository", "Trying to fetch buildings from network")
                val response = ApiClient.apiService.getAllBuildings()
                if (response.isSuccessful && response.body() != null) {
                    val buildings = response.body()!!
                    Log.d("BuildingRepository", "Successfully fetched ${buildings.size} buildings from network")
                    
                    // Guardar en la base de datos local
                    val buildingEntities = buildings.map { BuildingEntity.fromModel(it) }
                    buildingDao.insertBuildings(buildingEntities)
                    
                    return@withContext buildings
                } else {
                    Log.e("BuildingRepository", "Error fetching buildings: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BuildingRepository", "Exception fetching buildings", e)
            }
        } else {
            Log.d("BuildingRepository", "No internet connection, using cached data")
        }
        
        // Si no hay internet o hubo un error, cargar desde caché
        try {
            val cachedBuildings = buildingDao.getAllBuildings().first()
            Log.d("BuildingRepository", "Loaded ${cachedBuildings.size} buildings from cache")
            return@withContext cachedBuildings.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error loading from cache", e)
            return@withContext emptyList()
        }
    }

    suspend fun getBuildingById(id: Long): Building? = withContext(Dispatchers.IO) {
        // Primero intentar cargar desde red si hay conexión
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getBuildingById(id)
                if (response.isSuccessful && response.body() != null) {
                    val building = response.body()!!
                    Log.d("BuildingRepository", "Successfully fetched building with ID $id from network")
                    
                    // Guardar en caché
                    buildingDao.insertBuilding(BuildingEntity.fromModel(building))
                    
                    return@withContext building
                } else {
                    Log.e("BuildingRepository", "Error fetching building $id: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BuildingRepository", "Exception fetching building $id", e)
            }
        }
        
        // Si no hay red o hubo error, intentar desde caché
        try {
            val cachedBuilding = buildingDao.getBuildingById(id)
            if (cachedBuilding != null) {
                Log.d("BuildingRepository", "Loaded building $id from cache")
                return@withContext cachedBuilding.toModel()
            }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error loading building $id from cache", e)
        }
        
        return@withContext null
    }

    suspend fun getBuildingsByCategory(category: String): List<Building> = withContext(Dispatchers.IO) {
        // Primero intentar desde la red si hay conexión
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getBuildingsByCategory(category)
                if (response.isSuccessful && response.body() != null) {
                    val buildings = response.body()!!
                    Log.d("BuildingRepository", "Successfully fetched ${buildings.size} buildings for category $category from network")
                    
                    // Guardar en caché
                    val buildingEntities = buildings.map { BuildingEntity.fromModel(it) }
                    buildingDao.insertBuildings(buildingEntities)
                    
                    return@withContext buildings
                } else {
                    Log.e("BuildingRepository", "Error fetching buildings by category $category: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BuildingRepository", "Exception fetching buildings by category $category", e)
            }
        }
        
        // Si no hay red o hubo error, intentar desde caché
        try {
            val cachedBuildings = buildingDao.getBuildingsByCategory(category).first()
            Log.d("BuildingRepository", "Loaded ${cachedBuildings.size} buildings for category $category from cache")
            return@withContext cachedBuildings.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error loading buildings by category $category from cache", e)
            return@withContext emptyList()
        }
    }

    suspend fun getNearbyBuildings(latitude: Double, longitude: Double): List<Building> = withContext(Dispatchers.IO) {
        // Esta función solo funciona con red, pero mostrará resultados alternativos cuando no hay red
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getNearbyBuildings(latitude, longitude)
                if (response.isSuccessful && response.body() != null) {
                    val buildings = response.body()!!
                    Log.d("BuildingRepository", "Successfully fetched ${buildings.size} nearby buildings")
                    return@withContext buildings
                } else {
                    Log.e("BuildingRepository", "Error fetching nearby buildings: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("BuildingRepository", "Exception fetching nearby buildings", e)
            }
        } else {
            Log.d("BuildingRepository", "No internet for nearby buildings, showing all buildings instead")
        }
        
        // Si no hay conexión o hubo error, mostrar todos los edificios en caché
        try {
            val cachedBuildings = buildingDao.getAllBuildings().first()
            Log.d("BuildingRepository", "Showing ${cachedBuildings.size} cached buildings instead of nearby")
            return@withContext cachedBuildings.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Error loading cached buildings", e)
            return@withContext emptyList()
        }
    }
}