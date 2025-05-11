package com.example.explorandes.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.MapActivity
import com.example.explorandes.R
import com.example.explorandes.adapters.CategoryAdapter
import com.example.explorandes.adapters.PlaceAdapter
import com.example.explorandes.models.Category
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar

class NavigationFragment : Fragment() {

    private lateinit var viewModel: NavigationViewModel
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchView: SearchView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var emptyView: TextView
    private lateinit var noConnectionView: View
    private lateinit var retryButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation, container, false)

        // Initialize ViewModel with context
        viewModel = ViewModelProvider(
            this, 
            NavigationViewModel.Factory(requireContext())
        )[NavigationViewModel::class.java]

        // Initialize UI components
        val placesRecyclerView = view.findViewById<RecyclerView>(R.id.places_recyclerview)
        val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.categories_recyclerview)
        searchView = view.findViewById(R.id.search_view)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        emptyView = view.findViewById(R.id.empty_view)
        
        // Find or add the no connection view
        noConnectionView = view.findViewById(R.id.no_connection_view) ?: 
            inflater.inflate(R.layout.layout_no_connection, container, false).also {
                (view as ViewGroup).addView(it)
                it.visibility = View.GONE
            }
            
        retryButton = noConnectionView.findViewById(R.id.retry_button)
        retryButton.setOnClickListener {
            if (viewModel.checkConnectivity()) {
                viewModel.loadPlaces()
                showContent()
            } else {
                Toast.makeText(context, "Still no internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        val mapButton = view.findViewById<Button>(R.id.view_map_button)
        mapButton.setOnClickListener {
            // Start MapActivity without specific building
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivity(intent)
        }

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
            
            if (places.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                view?.findViewById<RecyclerView>(R.id.places_recyclerview)?.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                view?.findViewById<RecyclerView>(R.id.places_recyclerview)?.visibility = View.VISIBLE
            }
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
                if (it.contains("internet") || it.contains("connection")) {
                    // Show offline indicator if the error is connectivity-related
                    showOfflineIndicator(it)
                } else {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Observe connectivity status
        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                showContent()
                // Show a snackbar if connectivity was restored
                Snackbar.make(
                    requireView(),
                    "Internet connection restored",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                // Only show offline indicator when we have no data
                if (placeAdapter.itemCount == 0) {
                    showOfflineIndicator("No internet connection. Showing cached data.")
                }
            }
        }
    }
    
    private fun showOfflineIndicator(message: String) {
        val mainContent = view?.findViewById<View>(R.id.navigation_content)
        mainContent?.visibility = View.GONE
        noConnectionView.visibility = View.VISIBLE
        
        // Show a toast with the specific error message
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showContent() {
        val mainContent = view?.findViewById<View>(R.id.navigation_content)
        mainContent?.visibility = View.VISIBLE
        noConnectionView.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Check connectivity when fragment becomes visible
        viewModel.checkConnectivity()
        
        // If we're offline but there's data available, show a snackbar
        if (!viewModel.isConnected.value!! && placeAdapter.itemCount > 0) {
            Snackbar.make(
                requireView(),
                "You're offline. Showing cached data.",
                Snackbar.LENGTH_LONG
            ).setAction("Retry") {
                viewModel.checkConnectivity()
                if (viewModel.isConnected.value == true) {
                    viewModel.loadPlaces()
                }
            }.show()
        }
    }
}