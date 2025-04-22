package com.example.explorandes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Building
import com.example.explorandes.models.Place
import com.example.explorandes.repositories.BuildingRepository
import com.example.explorandes.repositories.PlaceRepository
import kotlinx.coroutines.launch
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.MapFragment
import android.os.Bundle

class BuildingDetailViewModel : ViewModel() {

    private val buildingRepository = BuildingRepository()
    private val placeRepository = PlaceRepository()

    private val _building = MutableLiveData<Building>()
    val building: LiveData<Building> = _building

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

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
}