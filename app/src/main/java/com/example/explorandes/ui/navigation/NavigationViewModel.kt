package com.example.explorandes.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Category
import com.example.explorandes.models.Place
import com.example.explorandes.repositories.BuildingRepository
import com.example.explorandes.repositories.PlaceRepository
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {

    private val buildingRepository = BuildingRepository()
    private val placeRepository = PlaceRepository()

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCategories()
        loadPlaces()
    }

    private fun loadCategories() {
        // Updated with exact categories from the database and appropriate icons
        _categories.value = listOf(
            Category(1, "All", com.example.explorandes.R.drawable.ic_all),
            Category(2, "Laboratory", com.example.explorandes.R.drawable.ic_laboratory),
            Category(3, "Study Area", com.example.explorandes.R.drawable.ic_study_area),
            Category(4, "Common Area", com.example.explorandes.R.drawable.ic_common_area),
            Category(5, "Sports", com.example.explorandes.R.drawable.ic_sports),
            Category(6, "Connection", com.example.explorandes.R.drawable.ic_connection),
            Category(7, "Other", com.example.explorandes.R.drawable.ic_other)
        )
    }

    fun loadPlaces() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val allPlaces = placeRepository.getAllPlaces()
                _places.value = allPlaces
            } catch (e: Exception) {
                _error.value = "Failed to load places: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterPlacesByCategory(category: Category) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val filteredPlaces = if (category.name == "All") {
                    placeRepository.getAllPlaces()
                } else {
                    // Using the exact category name from the database for filtering
                    placeRepository.getPlacesByCategory(category.name)
                }

                _places.value = filteredPlaces
            } catch (e: Exception) {
                _error.value = "Failed to filter places: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val searchResults = if (query.isEmpty()) {
                    placeRepository.getAllPlaces()
                } else {
                    placeRepository.searchPlaces(query)
                }

                _places.value = searchResults
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPlace(place: Place) {
        _selectedPlace.value = place
    }

    fun loadPlacesByBuilding(buildingId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val buildingPlaces = placeRepository.getPlacesByBuilding(buildingId)
                _places.value = buildingPlaces
            } catch (e: Exception) {
                _error.value = "Failed to load places for building: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}