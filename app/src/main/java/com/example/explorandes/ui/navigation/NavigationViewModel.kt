package com.example.explorandes.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.explorandes.R
import com.example.explorandes.models.Category
import com.example.explorandes.models.Place
import com.example.explorandes.models.UserLocation

class NavigationViewModel : ViewModel() {
    
    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places
    
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    
    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace
    
    // Mock data using existing drawable resources
    private val allPlaces = listOf(
        Place(1, "CupiTaller", "ML-501", "Laboratories", "50m", "7.38, -72.37", R.drawable.ic_building),
        Place(2, "Laboratorio Colivri", "ML-301", "Laboratories", "100m", "7.39, -72.38", R.drawable.ic_building),
        Place(3, "Waira", "ML-701", "Stores", "150m", "7.40, -72.39", R.drawable.ic_food),
        Place(4, "Computer Lab", "ML-101", "Computer Labs", "75m", "7.37, -72.36", R.drawable.ic_services),
        Place(5, "Auditorium", "ML-201", "Auditoriums", "200m", "7.41, -72.40", R.drawable.ic_study)
    )
    
    private val allCategories = listOf(
        Category(1, "Laboratories", R.drawable.ic_building),
        Category(2, "Auditoriums", R.drawable.ic_study),
        Category(3, "Stores", R.drawable.ic_food),
        Category(4, "Computer Labs", R.drawable.ic_services)
    )
    
    init {
        _places.value = allPlaces
        _categories.value = allCategories
    }
    
    fun filterPlacesByCategory(category: Category) {
        if (category.name == "All") {
            _places.value = allPlaces
        } else {
            _places.value = allPlaces.filter { it.category == category.name }
        }
    }
    
    fun searchPlaces(query: String) {
        if (query.isEmpty()) {
            _places.value = allPlaces
        } else {
            _places.value = allPlaces.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.code.contains(query, ignoreCase = true)
            }
        }
    }
    
    fun selectPlace(place: Place) {
        _selectedPlace.value = place
    }
}