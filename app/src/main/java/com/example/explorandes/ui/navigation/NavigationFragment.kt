package com.example.explorandes.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.R
import com.example.explorandes.adapters.CategoryAdapter
import com.example.explorandes.adapters.PlaceAdapter
import com.example.explorandes.models.Category
import com.google.android.material.progressindicator.CircularProgressIndicator

class NavigationFragment : Fragment() {

    private lateinit var viewModel: NavigationViewModel
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchView: SearchView
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(NavigationViewModel::class.java)

        // Initialize UI components
        val placesRecyclerView = view.findViewById<RecyclerView>(R.id.places_recyclerview)
        val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.categories_recyclerview)
        searchView = view.findViewById(R.id.search_view)
        progressIndicator = view.findViewById(R.id.progress_indicator)

        // Setup RecyclerViews
        setupPlacesRecyclerView(placesRecyclerView)
        setupCategoriesRecyclerView(categoriesRecyclerView)
        setupSearchView()

        // Observe ViewModel data
        observeViewModel()

        return view
    }

    private fun setupPlacesRecyclerView(recyclerView: RecyclerView) {
        placeAdapter = PlaceAdapter(emptyList()) { place ->
            viewModel.selectPlace(place)
            // Navigate to place detail or show in map
            Toast.makeText(context, "Selected: ${place.name}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = placeAdapter
        }
    }

    private fun setupCategoriesRecyclerView(recyclerView: RecyclerView) {
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            viewModel.filterPlacesByCategory(category)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchPlaces(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadPlaces()
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.places.observe(viewLifecycleOwner) { places ->
            placeAdapter.updateData(places)
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.updateData(categories)
        }

        viewModel.selectedPlace.observe(viewLifecycleOwner) { place ->
            // Handle selected place (e.g., navigate to detail screen)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}