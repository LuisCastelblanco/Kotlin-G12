package com.example.explorandes.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.explorandes.utils.ConnectivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.explorandes.database.AppDatabase
import com.example.explorandes.database.entity.EventEntity


class HomeViewModel(private val context: Context) : ViewModel() {
    private val buildingRepository = BuildingRepository(context)

    private val userRepository = UserRepository()

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
    
    // Add connectivity status LiveData
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    // Check initial connectivity
    init {
        checkConnectivity()
    }
    
    fun checkConnectivity(): Boolean {
        val connectivityHelper = ConnectivityHelper(context)
        val isAvailable = connectivityHelper.isInternetAvailable()
        _isConnected.value = isAvailable
        return isAvailable
    }

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
                
                Log.d("HomeViewModel", "Starting to load events, internet available: ${buildingRepository.isInternetAvailable()}")
                
                // Check connectivity before making API call
                if (buildingRepository.isInternetAvailable()) {
                    Log.d("HomeViewModel", "Attempting API call to fetch events")
                    val response = ApiClient.apiService.getAllEvents()
                    
                    Log.d("HomeViewModel", "API response received: code=${response.code()}, successful=${response.isSuccessful}")
                    
                    if (response.isSuccessful) {
                        val eventsFromNetwork = response.body()
                        Log.d("HomeViewModel", "Events from network: ${eventsFromNetwork?.size ?: "null"}")
                        
                        if (eventsFromNetwork != null) {
                            _events.value = eventsFromNetwork
                            Log.d("HomeViewModel", "LiveData updated with ${eventsFromNetwork.size} events")
                            
                            // Save to local database
                            saveEventsToLocalDatabase(eventsFromNetwork)
                        } else {
                            Log.e("HomeViewModel", "API returned successful response but body was null")
                            _error.value = "Server returned empty response"
                            loadEventsFromLocalDatabase()
                        }
                    } else {
                        Log.e("HomeViewModel", "API error: ${response.code()} - ${response.errorBody()?.string()}")
                        _error.value = "Failed to load events: ${response.code()} ${response.message()}"
                        loadEventsFromLocalDatabase()
                    }
                } else {
                    Log.d("HomeViewModel", "No internet connection, loading from local database")
                    _error.value = "No internet connection. Showing cached events."
                    loadEventsFromLocalDatabase()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception during event loading", e)
                _error.value = "Failed to load events: ${e.localizedMessage}"
                loadEventsFromLocalDatabase()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveEventsToLocalDatabase(events: List<Event>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val eventEntities = events.map { EventEntity.fromModel(it) }
                db.eventDao().insertEvents(eventEntities)
                Log.d("HomeViewModel", "Saved ${events.size} events to local database")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error saving events to local database", e)
            }
        }
    }

    private fun loadEventsFromLocalDatabase() {
        viewModelScope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.eventDao().getAllEvents().collect { eventEntities ->
                    val events = eventEntities.map { entity -> entity.toModel() }

                    _events.value = events
                    Log.d("HomeViewModel", "Loaded ${events.size} events from local database")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading events from local database", e)
                _events.value = emptyList()
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
    
    // Factory class to create HomeViewModel instances with context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}