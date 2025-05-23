package com.example.explorandes.repositories

import android.content.Context
import android.util.Log
import com.example.explorandes.api.ApiClient
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.database.entity.PlaceEntity
import com.example.explorandes.models.Place
import com.example.explorandes.utils.ConnectivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaceRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val placeDao = db.placeDao()
    private val connectivityHelper = ConnectivityHelper(context)

    fun isInternetAvailable(): Boolean {
        return connectivityHelper.isInternetAvailable()
    }

    suspend fun getAllPlaces(): List<Place> = withContext(Dispatchers.IO) {
        // First try to get from network if internet is available
        if (connectivityHelper.isInternetAvailable()) {
            try {
                Log.d("PlaceRepository", "Fetching places from network")
                val response = ApiClient.apiService.getAllPlaces()
                if (response.isSuccessful && response.body() != null) {
                    val places = response.body()!!
                    Log.d("PlaceRepository", "Successfully fetched ${places.size} places from network")
                    
                    // Save to local database
                    val placeEntities = places.map { PlaceEntity.fromModel(it) }
                    placeDao.insertPlaces(placeEntities)
                    
                    return@withContext places
                } else {
                    Log.e("PlaceRepository", "Error fetching places: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Exception fetching places", e)
            }
        } else {
            Log.d("PlaceRepository", "No internet connection, using cached data")
        }
        
        // If network request failed or no internet, load from cache
        try {
            val cachedPlaces = placeDao.getAllPlaces().first()
            Log.d("PlaceRepository", "Loaded ${cachedPlaces.size} places from cache")
            return@withContext cachedPlaces.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error loading from cache", e)
            return@withContext emptyList()
        }
    }

    suspend fun getPlaceById(id: Long): Place? = withContext(Dispatchers.IO) {
        // First try network if available
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getPlaceById(id)
                if (response.isSuccessful && response.body() != null) {
                    val place = response.body()!!
                    Log.d("PlaceRepository", "Successfully fetched place with ID $id from network")
                    
                    // Save to cache
                    placeDao.insertPlace(PlaceEntity.fromModel(place))
                    
                    return@withContext place
                } else {
                    Log.e("PlaceRepository", "Error fetching place $id: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Exception fetching place $id", e)
            }
        }
        
        // If network request failed or no internet, try cache
        try {
            val cachedPlace = placeDao.getPlaceById(id)
            if (cachedPlace != null) {
                Log.d("PlaceRepository", "Loaded place $id from cache")
                return@withContext cachedPlace.toModel()
            }
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error loading place $id from cache", e)
        }
        
        return@withContext null
    }

    suspend fun getPlacesByBuilding(buildingId: Long): List<Place> = withContext(Dispatchers.IO) {
        // Try network first if available
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getPlacesByBuilding(buildingId)
                if (response.isSuccessful && response.body() != null) {
                    val places = response.body()!!
                    Log.d("PlaceRepository", "Successfully fetched ${places.size} places for building $buildingId from network")
                    
                    // Save to cache
                    val placeEntities = places.map { PlaceEntity.fromModel(it) }
                    placeDao.insertPlaces(placeEntities)
                    
                    return@withContext places
                } else {
                    Log.e("PlaceRepository", "Error fetching places for building $buildingId: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Exception fetching places for building $buildingId", e)
            }
        }
        
        // If network failed or no internet, try cache
        try {
            val cachedPlaces = placeDao.getPlacesByBuilding(buildingId).first()
            Log.d("PlaceRepository", "Loaded ${cachedPlaces.size} places for building $buildingId from cache")
            return@withContext cachedPlaces.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error loading places for building $buildingId from cache", e)
            return@withContext emptyList()
        }
    }

    suspend fun getPlacesByCategory(category: String): List<Place> = withContext(Dispatchers.IO) {
        // Try network first if available
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.getPlacesByCategory(category)
                if (response.isSuccessful && response.body() != null) {
                    val places = response.body()!!
                    Log.d("PlaceRepository", "Successfully fetched ${places.size} places for category $category from network")
                    
                    // Save to cache
                    val placeEntities = places.map { PlaceEntity.fromModel(it) }
                    placeDao.insertPlaces(placeEntities)
                    
                    return@withContext places
                } else {
                    Log.e("PlaceRepository", "Error fetching places by category $category: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Exception fetching places by category $category", e)
            }
        }
        
        // If network failed or no internet, try cache
        try {
            val cachedPlaces = placeDao.getPlacesByCategory(category).first()
            Log.d("PlaceRepository", "Loaded ${cachedPlaces.size} places for category $category from cache")
            return@withContext cachedPlaces.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error loading places by category $category from cache", e)
            return@withContext emptyList()
        }
    }

    suspend fun searchPlaces(query: String): List<Place> = withContext(Dispatchers.IO) {
        // If we have internet, try API search
        if (connectivityHelper.isInternetAvailable()) {
            try {
                val response = ApiClient.apiService.searchPlaces(query)
                if (response.isSuccessful && response.body() != null) {
                    val places = response.body()!!
                    Log.d("PlaceRepository", "Successfully searched for '$query' and found ${places.size} places from network")
                    return@withContext places
                } else {
                    Log.e("PlaceRepository", "Error searching places for '$query': ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Exception searching places for '$query'", e)
            }
        }
        
        // If network failed or no internet, search local database
        try {
            val cachedPlaces = placeDao.searchPlaces(query).first()
            Log.d("PlaceRepository", "Searched for '$query' in cache and found ${cachedPlaces.size} places")
            return@withContext cachedPlaces.map { it.toModel() }
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error searching places in cache for '$query'", e)
            return@withContext emptyList()
        }
    }
}