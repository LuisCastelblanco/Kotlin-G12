package com.example.explorandes.ui.buildings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.BuildingDetailActivity
import com.example.explorandes.MapActivity
import com.example.explorandes.R
import com.example.explorandes.adapters.BuildingAdapter
import com.example.explorandes.viewmodels.HomeViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator

class BuildingsListFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var buildingAdapter: BuildingAdapter
    private lateinit var buildingsRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buildings_list, container, false)

        // Initialize views
        buildingsRecyclerView = view.findViewById(R.id.buildings_recycler)
        searchView = view.findViewById(R.id.search_view)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        toolbar = view.findViewById(R.id.toolbar)

        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Setup RecyclerView - use vertical layout for better list display
        buildingsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter with empty list
        // Initialize adapter with empty list
        buildingAdapter = BuildingAdapter(
            emptyList(),
            onBuildingClicked = { building ->
                // Navigate to building details
                val intent = Intent(context, BuildingDetailActivity::class.java).apply {
                    putExtra("BUILDING_ID", building.id)
                }
                startActivity(intent)
            }
        )

        buildingsRecyclerView.adapter = buildingAdapter

        // Setup search
        setupSearch()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) {
                    viewModel.searchBuildings(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    viewModel.loadBuildings()
                }
                return true
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the ViewModel from the activity
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        // Observe buildings data
        viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
            if (buildings.isEmpty()) {
                view.findViewById<View>(R.id.empty_view).visibility = View.VISIBLE
                buildingsRecyclerView.visibility = View.GONE
            } else {
                view.findViewById<View>(R.id.empty_view).visibility = View.GONE
                buildingsRecyclerView.visibility = View.VISIBLE
                buildingAdapter.updateData(buildings)
            }
            progressIndicator.visibility = View.GONE
        }

        // Load buildings data
        progressIndicator.visibility = View.VISIBLE
        viewModel.loadBuildings()
    }

    companion object {
        fun newInstance() = BuildingsListFragment()
    }
}