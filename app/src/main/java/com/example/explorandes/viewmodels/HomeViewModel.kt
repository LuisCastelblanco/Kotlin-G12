package com.example.explorandes.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.Building
import com.example.explorandes.models.Event
import com.example.explorandes.models.User
import com.example.explorandes.models.UserLocation
import com.example.explorandes.repositories.BuildingRepository
import com.example.explorandes.repositories.UserRepository
import com.example.explorandes.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val buildingRepository = BuildingRepository()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _buildings = MutableLiveData<List<Building>>()
    val buildings: LiveData<List<Building>> = _buildings

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userLocation = MutableLiveData<UserLocation>()
    val userLocation: LiveData<UserLocation> = _userLocation

    fun loadUserData(sessionManager: SessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userId = sessionManager.getUserId()
                Log.d("HomeViewModel", "Loading user data for ID: $userId")

                if (userId > 0) {
                    val userData = userRepository.getUserById(userId)
                    userData?.let {
                        _user.value = it
                        Log.d("HomeViewModel", "User data loaded: ${it.username}")
                    } ?: run {
                        _error.value = "Failed to load user data: User not found"
                        Log.e("HomeViewModel", "User not found for ID: $userId")
                    }
                } else {
                    _error.value = "No user ID available"
                    Log.e("HomeViewModel", "No valid user ID to load data")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load user data: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error loading user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBuildings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Loading all buildings")

                val allBuildings = buildingRepository.getAllBuildings()
                _buildings.value = allBuildings
                Log.d("HomeViewModel", "Loaded ${allBuildings.size} buildings")
            } catch (e: Exception) {
                _error.value = "Failed to load buildings: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error loading buildings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Loading events...")

                val response = ApiClient.apiService.getAllEvents()
                if (response.isSuccessful) {
                    _events.value = response.body()
                    Log.d("HomeViewModel", "Loaded ${response.body()?.size ?: 0} events")
                } else {
                    _error.value = "Failed to load events: ${response.code()} ${response.message()}"
                    Log.e("HomeViewModel", "Error loading events: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load events: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error loading events", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadBuildingsByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Loading buildings by category: $category")

                val filteredBuildings = buildingRepository.getBuildingsByCategory(category)
                _buildings.value = filteredBuildings
                Log.d("HomeViewModel", "Loaded ${filteredBuildings.size} buildings for category: $category")
            } catch (e: Exception) {
                _error.value = "Failed to load buildings by category: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error loading buildings by category", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNearbyBuildings(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Loading nearby buildings at lat:$latitude, lon:$longitude")

                val nearbyBuildings = buildingRepository.getNearbyBuildings(latitude, longitude)
                _buildings.value = nearbyBuildings
                Log.d("HomeViewModel", "Loaded ${nearbyBuildings.size} nearby buildings")
            } catch (e: Exception) {
                _error.value = "Failed to load nearby buildings: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error loading nearby buildings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = UserLocation(latitude, longitude)
        Log.d("HomeViewModel", "User location updated to lat:$latitude, lon:$longitude")
    }

    fun searchBuildings(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Searching buildings with query: $query")

                val searchResults = if (query.isEmpty()) {
                    buildingRepository.getAllBuildings()
                } else {
                    buildingRepository.getAllBuildings().filter {
                        it.name.contains(query, ignoreCase = true) ||
                                (it.description?.contains(query, ignoreCase = true) ?: false)
                    }
                }

                _buildings.value = searchResults
                Log.d("HomeViewModel", "Search found ${searchResults.size} results")
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.localizedMessage}"
                Log.e("HomeViewModel", "Error during search", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
