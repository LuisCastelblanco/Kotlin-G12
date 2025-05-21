package com.example.explorandes.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.EventDetail
import com.example.explorandes.repositories.EventDetailRepository
import com.example.explorandes.utils.ConnectivityHelper
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventDetailViewModel(private val context: Context) : ViewModel() {

    private val repository = EventDetailRepository(context)
    private val connectivityHelper = ConnectivityHelper(context)

    private val _eventDetail = MutableLiveData<NetworkResult<EventDetail>>()
    val eventDetail: LiveData<NetworkResult<EventDetail>> = _eventDetail

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    init {
        checkConnectivity()
    }

    fun checkConnectivity(): Boolean {
        val isAvailable = connectivityHelper.isInternetAvailable()
        _isConnected.value = isAvailable
        return isAvailable
    }

    fun loadEventDetail(eventId: Long) {
        viewModelScope.launch {
            repository.getEventDetail(eventId).collectLatest { result ->
                _eventDetail.value = result
            }
        }
    }

    fun saveEventDetail(eventDetail: EventDetail) {
        viewModelScope.launch {
            repository.saveEventDetail(eventDetail)
            _syncStatus.value = if (isConnected.value == true) "synced" else "pending_sync"
        }
    }

    fun syncPendingEvents() {
        if (!isConnected.value!!) return
        
        viewModelScope.launch {
            repository.syncPendingEvents()
            _syncStatus.value = "sync_completed"
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
                return EventDetailViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}