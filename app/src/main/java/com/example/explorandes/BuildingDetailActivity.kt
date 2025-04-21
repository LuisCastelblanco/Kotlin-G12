package com.example.explorandes

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.explorandes.adapters.PlaceAdapter
import com.example.explorandes.models.Building
import com.example.explorandes.models.Place
import com.example.explorandes.viewmodels.BuildingDetailViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.view.Window

class BuildingDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: BuildingDetailViewModel
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var buildingImage: ImageView
    private lateinit var buildingName: TextView
    private lateinit var buildingDescription: TextView
    private lateinit var buildingCode: TextView
    private lateinit var placesRecyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var toolbar: MaterialToolbar
    private lateinit var noPlacesMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_building_detail)

        // Get the building ID from the intent
        val buildingId = intent.getLongExtra("BUILDING_ID", -1L)
        if (buildingId == -1L) {
            Toast.makeText(this, "Building ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(BuildingDetailViewModel::class.java)

        // Initialize views
        initializeViews()

        // Setup observers
        setupObservers()

        // Load building data
        viewModel.loadBuilding(buildingId)
        viewModel.loadPlacesForBuilding(buildingId)
    }

    private fun initializeViews() {
        // Find views
        buildingImage = findViewById(R.id.building_image)
        buildingName = findViewById(R.id.building_name)
        buildingDescription = findViewById(R.id.building_description)
        buildingCode = findViewById(R.id.building_code)
        placesRecyclerView = findViewById(R.id.places_recyclerview)
        progressIndicator = findViewById(R.id.progress_indicator)
        toolbar = findViewById(R.id.toolbar)
        noPlacesMessage = findViewById(R.id.no_places_message)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup places RecyclerView
        placesRecyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = PlaceAdapter(emptyList()) { place ->
            // Handle place click
            Toast.makeText(this, "Selected place: ${place.name}", Toast.LENGTH_SHORT).show()
            // Here you could navigate to place details or show it on a map
        }
        placesRecyclerView.adapter = placeAdapter
    }

    private fun setupObservers() {
        // Observe building data
        viewModel.building.observe(this) { building ->
            updateBuildingUI(building)
        }

        // Observe places list
        viewModel.places.observe(this) { places ->
            updatePlacesUI(places)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBuildingUI(building: Building) {
        buildingName.text = building.name
        buildingDescription.text = building.description ?: "No description available"
        buildingCode.text = "Code: ${building.code}"

        // Load building image
        if (!building.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(building.imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(buildingImage)
        } else {
            buildingImage.setImageResource(R.drawable.profile_placeholder)
        }
    }

    private fun updatePlacesUI(places: List<Place>) {
        if (places.isEmpty()) {
            noPlacesMessage.visibility = View.VISIBLE
            placesRecyclerView.visibility = View.GONE
        } else {
            noPlacesMessage.visibility = View.GONE
            placesRecyclerView.visibility = View.VISIBLE
            placeAdapter.updateData(places)
        }
    }
}