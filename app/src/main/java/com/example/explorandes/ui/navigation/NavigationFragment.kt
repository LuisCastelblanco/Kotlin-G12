package com.example.explorandes.ui.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorandes.R
import com.example.explorandes.adapters.CategoriesAdapter
import com.example.explorandes.adapters.PlacesAdapter
import com.example.explorandes.databinding.FragmentNavigationBinding
import com.example.explorandes.models.Place
import com.example.explorandes.services.LocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class NavigationFragment : Fragment(), OnMapReadyCallback {
    
    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NavigationViewModel by viewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var locationService: LocationService
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupLocationAndMap()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        locationService = LocationService(requireContext())
        setupUI()
        initMap()
        checkLocationPermission()
    }
    
    private fun setupUI() {
        // Set up header title
        binding.headerTitle.text = "Bloque ML"
        
        // Set up categories recycler view
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        val categoriesAdapter = CategoriesAdapter { category ->
            viewModel.filterPlacesByCategory(category)
        }
        binding.categoriesRecyclerView.adapter = categoriesAdapter
        
        // Set up places recycler view
        binding.popularPlacesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        val placesAdapter = PlacesAdapter { place ->
            viewModel.selectPlace(place)
            switchToMapView()
        }
        binding.popularPlacesRecyclerView.adapter = placesAdapter
        
        // Set up view switching
        binding.filterButton.setOnClickListener {
            switchToMapView()
        }
        
        binding.backToPlacesButton.setOnClickListener {
            switchToPlacesView()
        }
        
        // Set up add to favorites
        binding.addToFavoritesButton.setOnClickListener {
            // Implement add to favorites functionality - future enhancement
        }
        
        // Set up search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchPlaces(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchPlaces(it) }
                return true
            }
        })
        
        // Steps button click listener
        binding.stepsButton.setOnClickListener {
            // Future enhancement: show step-by-step directions
        }
        
        // Observe view model data
        viewModel.places.observe(viewLifecycleOwner) { places ->
            (binding.popularPlacesRecyclerView.adapter as PlacesAdapter).submitList(places)
        }
        
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            (binding.categoriesRecyclerView.adapter as CategoriesAdapter).submitList(categories)
        }
        
        viewModel.selectedPlace.observe(viewLifecycleOwner) { place ->
            place?.let { updateMapForPlace(it) }
        }
    }
    
    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = true
        }
        
        // Default location - Universidad de los Andes (approximate)
        val uniandes = LatLng(4.6018, -74.0665)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uniandes, 15f))
        
        setupLocationAndMap()
    }
    
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupLocationAndMap()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user
                // You could show a dialog explaining why the permission is needed
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun setupLocationAndMap() {
        if (::googleMap.isInitialized && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            
            locationService.getLastLocation { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    
                    // If a place is already selected, update the map with it
                    viewModel.selectedPlace.value?.let { place ->
                        updateMapForPlace(place)
                    }
                }
            }
            
            locationService.startLocationUpdates()
        }
    }
    
    private fun updateMapForPlace(place: Place) {
        // Extract coordinates from the place
        val parts = place.coordinates.split(",")
        if (parts.size == 2) {
            val lat = parts[0].trim().toDoubleOrNull()
            val lng = parts[1].trim().toDoubleOrNull()
            
            if (lat != null && lng != null) {
                val placeLocation = LatLng(lat, lng)
                
                // Update marker
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(placeLocation)
                        .title(place.name)
                        .snippet(place.code)
                )
                
                // Get user location and create a route
                locationService.getLastLocation { userLocation ->
                    userLocation?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        
                        // Draw polyline between user and destination
                        val polylineOptions = PolylineOptions()
                            .add(userLatLng)
                            .add(placeLocation)
                            .width(5f)
                            .color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                        googleMap.addPolyline(polylineOptions)
                        
                        // Calculate distance
                        val distance = locationService.calculateDistance(
                            it.latitude, it.longitude, lat, lng
                        )
                        
                        // Calculate walking time (approx. 5km/h = 83.3m/min)
                        val walkingTimeMin = (distance / 83.3).toInt()
                        
                        // Update UI
                        binding.destinationNameText.text = place.name
                        binding.distanceText.text = "$walkingTimeMin min (${formatDistance(distance)})"
                        binding.coordinatesText.text = "${formatLatLng(lat)}, ${formatLatLng(lng)} Bogotá"
                        
                        // Zoom to show both points
                        val boundsBuilder = LatLngBounds.Builder()
                        boundsBuilder.include(userLatLng)
                        boundsBuilder.include(placeLocation)
                        val bounds = boundsBuilder.build()
                        
                        val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    } ?: run {
                        // If user location not available, just focus on the place
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 17f))
                    }
                }
            }
        }
    }
    
    private fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000)
        }
    }
    
    private fun formatLatLng(value: Double): String {
        return String.format("%.4f", value)
    }
    
    private fun switchToMapView() {
        binding.placesCardView.visibility = View.GONE
        binding.mapView.visibility = View.VISIBLE
        binding.mapViewControls.visibility = View.VISIBLE
    }
    
    private fun switchToPlacesView() {
        binding.mapView.visibility = View.GONE
        binding.mapViewControls.visibility = View.GONE
        binding.placesCardView.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        if (::locationService.isInitialized) {
            locationService.startLocationUpdates()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::locationService.isInitialized) {
            locationService.stopLocationUpdates()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}