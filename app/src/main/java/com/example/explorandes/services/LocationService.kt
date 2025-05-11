package com.example.explorandes.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.explorandes.models.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _currentLocation = MutableLiveData<UserLocation>()
    val currentLocation: LiveData<UserLocation> = _currentLocation

    // Improved location request with faster updates for better navigation
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Update every 5 seconds
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(2000) // Minimum 2 seconds between updates
        .setMaxUpdateDelayMillis(10000) // Maximum 10 seconds delay
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val userLocation = UserLocation(location.latitude, location.longitude)
                Log.d("LocationService", "Location update: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")
                _currentLocation.postValue(userLocation)
            }
        }
    }

    init {
        // Try to get last known location immediately upon initialization
        getLastLocation { location ->
            location?.let {
                Log.d("LocationService", "Initial location: ${it.latitude}, ${it.longitude}")
            } ?: Log.d("LocationService", "No initial location available")
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (hasLocationPermission()) {
            Log.d("LocationService", "Starting location updates")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.e("LocationService", "Location permission not granted")
        }
    }

    fun stopLocationUpdates() {
        Log.d("LocationService", "Stopping location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(callback: (UserLocation?) -> Unit) {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = UserLocation(it.latitude, it.longitude)
                        _currentLocation.postValue(userLocation)
                        callback(userLocation)
                    } ?: run {
                        Log.d("LocationService", "Last location is null")
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationService", "Error getting last location", e)
                    callback(null)
                }
        } else {
            Log.e("LocationService", "Location permission not granted")
            callback(null)
        }
    }

    // Calculate distance between user location and coordinates
    fun calculateDistance(userLat: Double, userLng: Double, destLat: Double, destLng: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, destLat, destLng, results)
        return results[0]  // Distance in meters
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}