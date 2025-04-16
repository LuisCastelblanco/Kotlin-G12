package com.example.explorandes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.User
import com.example.explorandes.api.ApiClient
import com.example.explorandes.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadUserData(sessionManager: SessionManager) {
        val userId = sessionManager.getUserId()
        
        if (userId <= 0) {
            _error.value = "No se encontró información de usuario"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getUserById(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    _user.value = response.body()
                } else {
                    _error.value = "Error al cargar el perfil: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}