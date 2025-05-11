package com.example.explorandes.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    private lateinit var noConnectionView: View

    private var selectedBuilding: Building? = null
    private var currentLocation: LatLng? = null
    private var offlineMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
): View? {  
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize ViewModel with context
        viewModel = ViewModelProvider(
            this,
            MapViewModel.Factory(requireContext())
        )[MapViewModel::class.java]

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
        
        // Fix the offline view setup - completely rewritten
        val inflatedOfflineView = layoutInflater.inflate(R.layout.layout_no_connection, null)
        inflatedOfflineView.visibility = View.GONE
        
        // Find and set the message
        val messageTextView = inflatedOfflineView.findViewById<android.widget.TextView>(R.id.no_connection_message)
        if (messageTextView != null) {
            messageTextView.text = "Internet connection is required for navigation.\nBasic map features are still available."
        }
        
        // Add to parent
        (view as? ViewGroup)?.addView(inflatedOfflineView)
        noConnectionView = inflatedOfflineView
        
        val retryButton = inflatedOfflineView.findViewById<Button>(R.id.retry_button)
        retryButton?.setOnClickListener {
            val connected = viewModel.checkConnectivity()
            if (connected) {
                offlineMode = false
                showMapContent()
                // Reload selected building if any
                selectedBuilding?.let { building ->
                    showRouteTo(building)
                }
            } else {
                Toast.makeText(context, "Still no internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up my location button
        val myLocationButton = view.findViewById<FloatingActionButton>(R.id.my_location_button)
        myLocationButton.setOnClickListener {
            focusOnUserLocation()
        }

        // Check if we received a buildingId in arguments
        arguments?.getLong("BUILDING_ID", -1L)?.let { buildingId ->
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
        
        // Observe connectivity status
        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            offlineMode = !isConnected
            
            if (isConnected) {
                showMapContent()
                // Show a short message
                Snackbar.make(
                    requireView(),
                    "Internet connection restored",
                    Snackbar.LENGTH_SHORT
                ).show()
                
                // Reload the route if a building is selected
                selectedBuilding?.let {
                    showRouteTo(it)
                }
            } else {
                // Only show the warning for routing features
                if (selectedBuilding != null) {
                    showOfflineIndicator()
                } else {
                    // For basic viewing, just show a snackbar
                    Snackbar.make(
                        requireView(),
                        "You're offline. Navigation features limited.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") {
                        viewModel.checkConnectivity()
                    }.show()
                }
            }
        }

        // Load buildings
        viewModel.loadBuildings()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set up map settings
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = true
        }

        // Check for location permission
        requestLocationPermissions()

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

        // Add styling to the map (optional)
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MapFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapFragment", "Can't find style. Error: ", e)
        } catch (e: Exception) {
            // If the style file doesn't exist, just continue without styling
            Log.e("MapFragment", "Error setting map style: ", e)
        }
        
        // Check connectivity status
        viewModel.checkConnectivity()
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            // Get current location immediately using FusedLocationProviderClient
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    updateUserMarker()

                    // If there's a selected building, update the route
                    selectedBuilding?.let { building ->
                        showRouteTo(building)
                    }
                }
            }

            // Start continuous location updates
            locationService.startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required for navigation",
                    Toast.LENGTH_LONG
                ).show()
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
    }

    private fun showRouteTo(building: Building) {
        // Check if we're in offline mode
        if (offlineMode) {
            showOfflineIndicator()
            return
        }
        
        // Get current location
        val currentLoc = currentLocation
        if (currentLoc == null) {
            Toast.makeText(context, "No se puede obtener su ubicación actual", Toast.LENGTH_SHORT).show()

            // Try to get location again
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val newCurrentLoc = LatLng(it.latitude, it.longitude)
                        currentLocation = newCurrentLoc
                        showRouteWithLocation(building, newCurrentLoc)
                    }
                }
                return
            } else {
                requestLocationPermissions()
                return
            }
        }

        showRouteWithLocation(building, currentLoc)
    }

    private fun showRouteWithLocation(building: Building, currentLoc: LatLng) {
        // Get destination
        val destination = LatLng(building.latitude, building.longitude)

        // Log coordinates for debugging
        Log.d("MapFragment", "Showing route from ${currentLoc.latitude},${currentLoc.longitude} to ${destination.latitude},${destination.longitude}")

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

                    if (directionsJson != null) {
                        // Draw the route on the map
                        val routeDrawn = DirectionsUtils.drawRouteFromJson(mMap, directionsJson)

                        if (routeDrawn) {
                            // Get route info
                            val (distance, duration) = DirectionsUtils.getRouteInfoFromJson(directionsJson)

                            // Update UI with route data
                            destinationNameTextView.text = building.name
                            routeInfoTextView.text = "Distancia: $distance"
                            estimatedTimeTextView.text = "Tiempo estimado: $duration"

                            // Configure the button to open Google Maps
                            view?.findViewById<Button>(R.id.start_navigation_button)?.setOnClickListener {
                                openGoogleMapsNavigation(building)
                            }

                            // Show the bottom sheet
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                            // Display step-by-step directions if available
                            displayStepByStepDirections(directionsJson)

                            // Zoom to show the entire route
                            val boundsBuilder = DirectionsUtils.getRouteBoundsFromJson(directionsJson)
                            if (boundsBuilder != null) {
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(
                                        boundsBuilder.build(),
                                        100 // padding
                                    )
                                )
                            } else {
                                // Fallback if we can't get the route bounds
                                val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                                    .include(currentLoc)
                                    .include(destination)
                                    .build()

                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                )
                            }
                        } else {
                            // If we couldn't draw the route, show fallback
                            drawFallbackRoute(currentLoc, destination, building)
                        }
                    } else {
                        // No directions data, show fallback
                        Log.e("MapFragment", "Failed to get directions data")
                        drawFallbackRoute(currentLoc, destination, building)
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    Log.e("MapFragment", "Error showing route", e)
                    drawFallbackRoute(currentLoc, destination, building)
                }
            }
        }
    }

    private fun displayStepByStepDirections(directionsJson: JSONObject) {
        try {
            val stepsContainer = view?.findViewById<LinearLayout>(R.id.steps_container)
            stepsContainer?.removeAllViews()

            val routes = directionsJson.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")
                if (legs.length() > 0) {
                    val leg = legs.getJSONObject(0)
                    val steps = leg.getJSONArray("steps")

                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val instruction = step.getString("html_instructions")
                        val distance = step.getJSONObject("distance").getString("text")

                        // Create a TextView for this step
                        val stepView = TextView(context)
                        stepView.text = Html.fromHtml("• $instruction ($distance)", Html.FROM_HTML_MODE_COMPACT)
                        stepView.textSize = 14f
                        stepView.setPadding(0, 8, 0, 8)

                        // Add to container
                        stepsContainer?.addView(stepView)

                        // Add a divider except for the last item
                        if (i < steps.length() - 1) {
                            val divider = View(context)
                            divider.setBackgroundColor(Color.LTGRAY)
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1
                            )
                            params.setMargins(0, 8, 0, 8)
                            divider.layoutParams = params
                            stepsContainer?.addView(divider)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MapFragment", "Error displaying step-by-step directions", e)
        }
    }

    private fun drawFallbackRoute(currentLoc: LatLng, destination: LatLng, building: Building) {
        Toast.makeText(
            context,
            "No se pudo obtener la ruta detallada. Mostrando línea directa.",
            Toast.LENGTH_SHORT
        ).show()

        val polylineOptions = PolylineOptions()
            .add(currentLoc, destination)
            .width(12f)
            .color(Color.BLUE)
            .geodesic(true)

        mMap.addPolyline(polylineOptions)

        // Calculate distance locally
        val distance = locationService.calculateDistance(
            currentLoc.latitude,
            currentLoc.longitude,
            building.latitude,
            building.longitude
        )

        destinationNameTextView.text = building.name
        routeInfoTextView.text = "Distancia: ${formatDistance(distance)}"
        estimatedTimeTextView.text = "Tiempo estimado: ${estimateTime(distance)}"

        // Set up the button
        view?.findViewById<Button>(R.id.start_navigation_button)?.setOnClickListener {
            openGoogleMapsNavigation(building)
        }

        // Show the bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Zoom to show both points
        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
            .include(currentLoc)
            .include(destination)
            .build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
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

            // Try to get the location again
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude),
                                17f
                            )
                        )
                    }
                }
            }
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

        // Factory method to create a new instance with arguments
        fun newInstance(buildingId: Long): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putLong("BUILDING_ID", buildingId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun showOfflineIndicator() {
        // For full-screen map, just show a snackbar
        Snackbar.make(
            requireView(),
            "You're offline. Navigation features are limited.",
            Snackbar.LENGTH_LONG
        ).setAction("Retry") {
            viewModel.checkConnectivity()
            if (viewModel.isConnected.value == true) {
                offlineMode = false
                showMapContent()
                // Attempt to reload the route
                selectedBuilding?.let { building ->
                    showRouteTo(building)
                }
            }
        }.show()
        
        // For navigation features, fall back to direct line
        selectedBuilding?.let { building ->
            currentLocation?.let { currentLoc ->
                drawFallbackRoute(currentLoc, LatLng(building.latitude, building.longitude), building)
            }
        }
    }
    
    private fun showMapContent() {
        noConnectionView.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Check connectivity status when fragment returns to foreground
        viewModel.checkConnectivity()
    }
}