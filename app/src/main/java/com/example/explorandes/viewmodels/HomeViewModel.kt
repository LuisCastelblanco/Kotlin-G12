package com.example.explorandes.viewmodels

import android.util.Log
import androidx.lifecycle.*
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userLocation = MutableLiveData<UserLocation>()
    val userLocation: LiveData<UserLocation> = _userLocation

    val events = MutableLiveData<List<Event>>()

    fun loadUserData(sessionManager: SessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userId = sessionManager.getUserId()
                if (userId > 0) {
                    val userData = userRepository.getUserById(userId)
                    userData?.let {
                        _user.value = it
                    } ?: run {
                        _error.value = "Usuario no encontrado"
                    }
                } else {
                    _error.value = "ID de usuario inválido"
                }
            } catch (e: Exception) {
                _error.value = "Error cargando usuario: ${e.localizedMessage}"
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
                _buildings.value = buildingRepository.getAllBuildings()
            } catch (e: Exception) {
                _error.value = "Error cargando edificios: ${e.localizedMessage}"
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
                _buildings.value = buildingRepository.getBuildingsByCategory(category)
            } catch (e: Exception) {
                _error.value = "Error filtrando edificios: ${e.localizedMessage}"
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
                _buildings.value = buildingRepository.getNearbyBuildings(latitude, longitude)
            } catch (e: Exception) {
                _error.value = "Error cargando edificios cercanos: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = UserLocation(latitude, longitude)
    }

    fun searchBuildings(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val all = buildingRepository.getAllBuildings()
                _buildings.value = all.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.description?.contains(query, ignoreCase = true) == true
                }
            } catch (e: Exception) {
                _error.value = "Error en búsqueda: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getAllEvents()
                if (response.isSuccessful) {
                    events.value = response.body()
                } else {
                    _error.value = "Error al cargar eventos: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
