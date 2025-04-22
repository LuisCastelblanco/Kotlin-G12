// app/src/main/java/com/example/explorandes/ui/map/MapFragment.kt
package com.example.explorandes.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.explorandes.R
import com.example.explorandes.models.Building
import com.example.explorandes.services.LocationService
import com.example.explorandes.utils.DirectionsUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.view.View

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: MapViewModel
    private lateinit var locationService: LocationService
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var routeInfoTextView: TextView
    private lateinit var destinationNameTextView: TextView
    private lateinit var estimatedTimeTextView: TextView
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var selectedBuilding: Building? = null
    private var currentLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        
        // Initialize LocationService
        locationService = LocationService(requireContext())

        // Get the SupportMapFragment and request notification when the map is ready
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up bottom sheet for route information
        bottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Get text views for route info
        destinationNameTextView = view.findViewById(R.id.destination_name)
        routeInfoTextView = view.findViewById(R.id.route_info)
        estimatedTimeTextView = view.findViewById(R.id.estimated_time)
        progressBar = view.findViewById(R.id.progress_bar)

        // Set up my location button
        val myLocationButton = view.findViewById<FloatingActionButton>(R.id.my_location_button)
        myLocationButton.setOnClickListener {
            focusOnUserLocation()
        }

        // Check if we received a buildingId in arguments
        arguments?.getLong("BUILDING_ID", -1)?.let { buildingId ->
            if (buildingId != -1L) {
                viewModel.selectBuildingById(buildingId)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe buildings data
        viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
            if (buildings.isNotEmpty() && ::mMap.isInitialized) {
                addBuildingsToMap(buildings)
            }
        }

        // Observe selected destination
        viewModel.selectedDestination.observe(viewLifecycleOwner) { building ->
            if (building != null && ::mMap.isInitialized) {
                selectedBuilding = building
                showRouteTo(building)
            }
        }

        // Observe user location
        locationService.currentLocation.observe(viewLifecycleOwner) { userLocation ->
            if (userLocation != null) {
                currentLocation = LatLng(userLocation.latitude, userLocation.longitude)
                if (::mMap.isInitialized) {
                    updateUserMarker()
                }
                
                // If there's a selected building, update the route
                selectedBuilding?.let {
                    showRouteTo(it)
                }
            }
        }

        // Load buildings
        viewModel.loadBuildings()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set up map settings
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            locationService.startLocationUpdates()
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Set default camera position to Universidad de los Andes campus
        val uniandes = LatLng(4.6025, -74.0665)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uniandes, 15f))

        // Setup map click listener
        mMap.setOnMarkerClickListener { marker ->
            val buildingId = marker.tag as? Long
            buildingId?.let {
                viewModel.selectBuildingById(it)
            }
            true
        }

        // If we already have buildings, add them to the map
        viewModel.buildings.value?.let { buildings ->
            if (buildings.isNotEmpty()) {
                addBuildingsToMap(buildings)
            }
        }
    }

    private fun addBuildingsToMap(buildings: List<Building>) {
        mMap.clear()
        
        // Add building markers
        for (building in buildings) {
            val position = LatLng(building.latitude, building.longitude)
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(building.name)
                    .snippet(building.description ?: "")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            marker?.tag = building.id
        }
        
        // Update user marker if we have location
        updateUserMarker()
    }

    private fun updateUserMarker() {
        currentLocation?.let { location ->
            // No need to add a separate marker as we're using the built-in my location layer
        }
    }

    private fun showRouteTo(building: Building) {
        // Get current location
        val currentLoc = currentLocation ?: return
        
        // Get destination
        val destination = LatLng(building.latitude, building.longitude)
        
        // Clear previous routes
        mMap.clear()
        
        // Re-add all building markers
        viewModel.buildings.value?.let { buildings ->
            addBuildingsToMap(buildings)
        }
        
        // Show loading state
        progressBar.visibility = View.VISIBLE
        
        // Get Google Maps API key from resources
        val apiKey = getString(R.string.google_maps_key)
        
        // Get directions from the API
        lifecycleScope.launch {
            try {
                val directionsJson = DirectionsUtils.getDirections(
                    requireContext(),
                    currentLoc,
                    destination,
                    apiKey
                )
                
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    // Dibujar la ruta en el mapa
                    val routeDrawn = DirectionsUtils.drawRouteFromJson(mMap, directionsJson)
                    
                    if (routeDrawn) {
                        // Obtener información de la ruta
                        val (distance, duration) = DirectionsUtils.getRouteInfoFromJson(directionsJson)
                        
                        // Actualizar la UI con los datos de la ruta
                        destinationNameTextView.text = building.name
                        routeInfoTextView.text = "Distancia: $distance"
                        estimatedTimeTextView.text = "Tiempo estimado: $duration"
                        
                        // Configurar el botón para abrir Google Maps
                        view?.findViewById<Button>(R.id.start_navigation_button)?.setOnClickListener {
                            openGoogleMapsNavigation(building)
                        }
                        
                        // Mostrar el bottom sheet
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        
                        // Hacer zoom para mostrar toda la ruta
                        val boundsBuilder = DirectionsUtils.getRouteBoundsFromJson(directionsJson)
                        if (boundsBuilder != null) {
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    boundsBuilder.build(),
                                    100 // padding
                                )
                            )
                        } else {
                            // Fallback si no se pueden obtener los límites de la ruta
                            val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                                .include(currentLoc)
                                .include(destination)
                                .build()
                            
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100)
                            )
                        }
                    } else {
                        // Si no se pudo dibujar la ruta, mostrar una línea recta
                        val polylineOptions = PolylineOptions()
                            .add(currentLoc, destination)
                            .width(12f)
                            .color(Color.BLUE)
                            .geodesic(true)
                        
                        mMap.addPolyline(polylineOptions)
                        
                        // Calcular distancia localmente
                        val distance = locationService.calculateDistance(
                            currentLoc.latitude, 
                            currentLoc.longitude,
                            building.latitude, 
                            building.longitude
                        )
                        
                        destinationNameTextView.text = building.name
                        routeInfoTextView.text = "Distancia: ${formatDistance(distance)}"
                        estimatedTimeTextView.text = "Tiempo estimado: ${estimateTime(distance)}"
                        
                        Toast.makeText(
                            context, 
                            "No se pudo obtener la ruta detallada. Mostrando línea directa.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    // Mostrar el bottom sheet en cualquier caso
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    // Error al obtener direcciones, mostrar línea recta
                    val polylineOptions = PolylineOptions()
                        .add(currentLoc, destination)
                        .width(12f)
                        .color(Color.BLUE)
                        .geodesic(true)
                    
                    mMap.addPolyline(polylineOptions)
                    
                    // Calcular distancia localmente
                    val distance = locationService.calculateDistance(
                        currentLoc.latitude, 
                        currentLoc.longitude,
                        building.latitude, 
                        building.longitude
                    )
                    
                    destinationNameTextView.text = building.name
                    routeInfoTextView.text = "Distancia: ${formatDistance(distance)}"
                    estimatedTimeTextView.text = "Tiempo estimado: ${estimateTime(distance)}"
                    
                    Toast.makeText(
                        context, 
                        "Error al obtener la ruta: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Mostrar el bottom sheet de todos modos
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    private fun openGoogleMapsNavigation(building: Building) {
        // Crear intent para abrir Google Maps con navegación
        val gmmIntentUri = Uri.parse(
            "google.navigation:q=${building.latitude},${building.longitude}&mode=w"
        )
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Asegurar que abre Google Maps
        
        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback en caso de que Google Maps no esté instalado
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${building.latitude},${building.longitude}&travelmode=walking")
            )
            startActivity(browserIntent)
        }
    }

    private fun focusOnUserLocation() {
        currentLocation?.let { location ->
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(location, 17f)
            )
        } ?: run {
            Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDistance(distanceInMeters: Float): String {
        return if (distanceInMeters < 1000) {
            "${distanceInMeters.toInt()} metros"
        } else {
            val distanceInKm = distanceInMeters / 1000
            String.format("%.1f km", distanceInKm)
        }
    }

    private fun estimateTime(distanceInMeters: Float): String {
        // Assuming walking speed of 5km/h or about 1.4m/s
        val walkingSpeedInMetersPerSecond = 1.4f
        val timeInSeconds = distanceInMeters / walkingSpeedInMetersPerSecond
        
        return when {
            timeInSeconds < 60 -> "${timeInSeconds.toInt()} segundos"
            timeInSeconds < 3600 -> "${(timeInSeconds / 60).toInt()} minutos"
            else -> {
                val hours = (timeInSeconds / 3600).toInt()
                val minutes = ((timeInSeconds % 3600) / 60).toInt()
                "$hours horas $minutes minutos"
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        
        // Factory method para crear una nueva instancia con argumentos
        fun newInstance(buildingId: Long): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putLong("BUILDING_ID", buildingId)
            fragment.arguments = args
            return fragment
        }
    }
}