// PlaceRepository.kt
package com.example.explorandes.repositories

import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.Place

class PlaceRepository {

    suspend fun getAllPlaces(): List<Place> {
        val response = ApiClient.apiService.getAllPlaces()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        return emptyList()
    }

    suspend fun getPlaceById(id: Long): Place? {
        val response = ApiClient.apiService.getPlaceById(id)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getPlacesByBuilding(buildingId: Long): List<Place> {
        val response = ApiClient.apiService.getPlacesByBuilding(buildingId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        return emptyList()
    }

    suspend fun getPlacesByCategory(category: String): List<Place> {
        val response = ApiClient.apiService.getPlacesByCategory(category)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        return emptyList()
    }

    suspend fun searchPlaces(query: String): List<Place> {
        val response = ApiClient.apiService.searchPlaces(query)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        return emptyList()
    }
}