package com.example.explorandes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explorandes.models.Event
import com.example.explorandes.repositories.EventRepository
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

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
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
            eventRepository.getEventsByType(type).collect { result ->
                _events.value = result
            }
        }
    }

    fun loadUpcomingEvents(limit: Int = 10) {
        viewModelScope.launch {
            _events.value = NetworkResult.Loading()
            eventRepository.getUpcomingEvents(limit).collect { result ->
                _events.value = result
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
            eventRepository.searchEvents(query).collect { result ->
                _events.value = result
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
            eventRepository.getEventsByLocation(locationId).collect { result ->
                _events.value = result
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