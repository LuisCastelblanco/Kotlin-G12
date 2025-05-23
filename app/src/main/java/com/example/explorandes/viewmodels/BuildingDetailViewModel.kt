package com.example.explorandes.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Building
import com.example.explorandes.models.Place
import com.example.explorandes.repositories.BuildingRepository
import com.example.explorandes.repositories.PlaceRepository
import kotlinx.coroutines.launch
import com.example.explorandes.utils.ConnectivityHelper

class BuildingDetailViewModel(private val context: Context) : ViewModel() {

    private val buildingRepository = BuildingRepository(context)
    private val placeRepository = PlaceRepository(context)

    private val _building = MutableLiveData<Building>()
    val building: LiveData<Building> = _building

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Add connectivity status LiveData
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected
    
    init {
        checkConnectivity()
    }
    
    fun checkConnectivity(): Boolean {
        val connectivityHelper = ConnectivityHelper(context)
        val isAvailable = connectivityHelper.isInternetAvailable()
        _isConnected.value = isAvailable
        return isAvailable
    }

    fun loadBuilding(buildingId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val buildingData = buildingRepository.getBuildingById(buildingId)
                buildingData?.let {
                    _building.value = it
                } ?: run {
                    _error.value = "Building not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load building: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlacesForBuilding(buildingId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val placesData = placeRepository.getPlacesByBuilding(buildingId)
                _places.value = placesData
            } catch (e: Exception) {
                _error.value = "Failed to load places: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Factory class to create BuildingDetailViewModel instances with context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BuildingDetailViewModel::class.java)) {
                return BuildingDetailViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}