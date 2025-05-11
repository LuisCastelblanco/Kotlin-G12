package com.example.explorandes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Event
import com.example.explorandes.repositories.EventRepository
import com.example.explorandes.utils.ConnectivityHelper
import com.example.explorandes.utils.NetworkResult
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _events = MutableLiveData<NetworkResult<List<Event>>>()
    val events: LiveData<NetworkResult<List<Event>>> = _events

    private val _selectedEvent = MutableLiveData<Event>()
    val selectedEvent: LiveData<Event> = _selectedEvent

    // For filtering
    private val _currentFilter = MutableLiveData<String?>(null)
    val currentFilter: LiveData<String?> = _currentFilter

    private val _allEvents = MutableLiveData<List<Event>>(emptyList())
    
    // For connectivity status
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    init {
        loadEvents()
        checkConnectivity()
    }
    
    fun checkConnectivity(): Boolean {
        val isAvailable = eventRepository.isInternetAvailable()
        _isConnected.value = isAvailable
        return isAvailable
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            
            eventRepository.getAllEvents().collect { result ->
                _events.value = result

                if (result is NetworkResult.Success) {
                    _allEvents.value = result.data ?: emptyList()
                    applyFilter(_currentFilter.value)
                }
            }
        }
    }

    fun loadEventById(id: Long) {
        viewModelScope.launch {
            eventRepository.getEventById(id).collect { result ->
                if (result is NetworkResult.Success) {
                    result.data?.let { event ->
                        _selectedEvent.value = event
                    }
                }
            }
        }
    }

    fun loadEventsByType(type: String) {
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            
            // Add a filter on the existing events if internet isn't available
            if (!eventRepository.isInternetAvailable()) {
                val filteredEvents = _allEvents.value?.filter { it.type == type } ?: emptyList()
                _events.value = NetworkResult.Success(filteredEvents)
                return@launch
            }
            
            // Try to get fresh data from repository
            try {
                // Make a custom search or filter on the existing data
                val events = _allEvents.value?.filter { it.type == type } ?: emptyList()
                _events.value = NetworkResult.Success(events)
            } catch (e: Exception) {
                _events.value = NetworkResult.Error("Error loading events by type: ${e.message}")
            }
        }
    }
    
    fun loadUpcomingEvents(limit: Int = 10) {
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            
            try {
                // Get all events and filter for upcoming ones
                val allEventsList = _allEvents.value ?: emptyList()
                val upcomingEvents = allEventsList.filter { 
                    it.isUpcoming() 
                }.take(limit)
                
                _events.value = NetworkResult.Success(upcomingEvents)
            } catch (e: Exception) {
                _events.value = NetworkResult.Error("Error loading upcoming events: ${e.message}")
            }
        }
    }
    
    fun searchEvents(query: String) {
        if (query.isBlank()) {
            loadEvents()
            return
        }
    
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            
            try {
                // Filter existing events by query
                val allEventsList = _allEvents.value ?: emptyList()
                val searchResults = allEventsList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) ?: false) ||
                    (it.locationName?.contains(query, ignoreCase = true) ?: false)
                }
                
                _events.value = NetworkResult.Success(searchResults)
            } catch (e: Exception) {
                _events.value = NetworkResult.Error("Error searching events: ${e.message}")
            }
        }
    }

    fun setSelectedEvent(event: Event) {
        _selectedEvent.value = event
    }

    fun applyFilter(filter: String?) {
        _currentFilter.value = filter

        val allEventsList = _allEvents.value ?: emptyList()

        if (filter.isNullOrEmpty()) {
            _events.value = NetworkResult.Success(allEventsList)
            return
        }

        val filteredList = when (filter) {
            Event.TYPE_EVENT, Event.TYPE_MOVIE, Event.TYPE_SPORTS -> {
                allEventsList.filter { it.type == filter }
            }
            "upcoming" -> {
                allEventsList.filter {
                    val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
                    val startTime = LocalDateTime.parse(it.startTime, formatter)
                    startTime.isAfter(LocalDateTime.now())
                }
            }
            "ongoing" -> {
                allEventsList.filter { event -> event.isHappeningNow() }
            }
            "past" -> {
                allEventsList.filter { event -> event.isPast() }
            }
            else -> allEventsList
        }

        _events.value = NetworkResult.Success(filteredList)
    }

    fun getEventsByLocation(locationId: Long) {
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            
            try {
                // Filter existing events by location ID
                val allEventsList = _allEvents.value ?: emptyList()
                val locationEvents = allEventsList.filter {
                    it.locationId == locationId
                }
                
                _events.value = NetworkResult.Success(locationEvents)
            } catch (e: Exception) {
                _events.value = NetworkResult.Error("Error loading events by location: ${e.message}")
            }
        }
    }

    // Factory class for creating EventViewModel instances
    class Factory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(eventRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}