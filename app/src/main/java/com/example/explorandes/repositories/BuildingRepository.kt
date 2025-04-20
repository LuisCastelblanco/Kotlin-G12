package com.example.explorandes.repositories

import android.util.Log
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.Building

class BuildingRepository {

    suspend fun getAllBuildings(): List<Building> {
        try {
            val response = ApiClient.apiService.getAllBuildings()
            if (response.isSuccessful && response.body() != null) {
                val buildings = response.body()!!
                Log.d("BuildingRepository", "Successfully fetched ${buildings.size} buildings")
                return buildings
            } else {
                Log.e("BuildingRepository", "Error fetching buildings: ${response.code()} - ${response.message()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Exception fetching buildings", e)
            return emptyList()
        }
    }

    suspend fun getBuildingById(id: Long): Building? {
        try {
            val response = ApiClient.apiService.getBuildingById(id)
            if (response.isSuccessful) {
                val building = response.body()
                Log.d("BuildingRepository", "Successfully fetched building with ID $id")
                return building
            } else {
                Log.e("BuildingRepository", "Error fetching building $id: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Exception fetching building $id", e)
            return null
        }
    }

    suspend fun getBuildingsByCategory(category: String): List<Building> {
        try {
            val response = ApiClient.apiService.getBuildingsByCategory(category)
            if (response.isSuccessful && response.body() != null) {
                val buildings = response.body()!!
                Log.d("BuildingRepository", "Successfully fetched ${buildings.size} buildings in category $category")
                return buildings
            } else {
                Log.e("BuildingRepository", "Error fetching buildings by category $category: ${response.code()} - ${response.message()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Exception fetching buildings by category $category", e)
            return emptyList()
        }
    }

    suspend fun getNearbyBuildings(latitude: Double, longitude: Double): List<Building> {
        try {
            val response = ApiClient.apiService.getNearbyBuildings(latitude, longitude)
            if (response.isSuccessful && response.body() != null) {
                val buildings = response.body()!!
                Log.d("BuildingRepository", "Successfully fetched ${buildings.size} nearby buildings")
                return buildings
            } else {
                Log.e("BuildingRepository", "Error fetching nearby buildings: ${response.code()} - ${response.message()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("BuildingRepository", "Exception fetching nearby buildings", e)
            return emptyList()
        }
    }
}