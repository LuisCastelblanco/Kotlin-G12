package com.example.explorandes.ui.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Building
import com.example.explorandes.repositories.BuildingRepository
import com.example.explorandes.models.UserLocation
import com.example.explorandes.utils.ConnectivityHelper
import kotlinx.coroutines.launch


class MapViewModel(private val context: Context) : ViewModel() {

    private val buildingRepository = BuildingRepository(context)

    private val _buildings = MutableLiveData<List<Building>>()
    val buildings: LiveData<List<Building>> = _buildings

    private val _selectedDestination = MutableLiveData<Building?>()
    val selectedDestination: LiveData<Building?> = _selectedDestination

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Add connectivity status LiveData
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected
    
    // Check connectivity status
    init {
        checkConnectivity()
    }
    
    fun checkConnectivity(): Boolean {
        val connectivityHelper = ConnectivityHelper(context)
        val isAvailable = connectivityHelper.isInternetAvailable()
        _isConnected.value = isAvailable
        return isAvailable
    }

    fun loadBuildings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val buildingsList = buildingRepository.getAllBuildings()
                _buildings.value = buildingsList
            } catch (e: Exception) {
                _error.value = "Failed to load buildings: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectBuildingById(buildingId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val building = buildingRepository.getBuildingById(buildingId)
                _selectedDestination.value = building
            } catch (e: Exception) {
                _error.value = "Failed to load building details: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectBuilding(building: Building) {
        _selectedDestination.value = building
    }

    fun clearSelectedDestination() {
        _selectedDestination.value = null
    }
    
    // Factory class for creating the ViewModel with context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}