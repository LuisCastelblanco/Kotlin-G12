package com.example.explorandes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorandes.EventDetailActivity
import com.example.explorandes.R
import com.example.explorandes.adapters.EventAdapter
import com.example.explorandes.api.ApiClient
import com.example.explorandes.databinding.FragmentEventListBinding
import com.example.explorandes.models.Event
import com.example.explorandes.repositories.EventRepository
import com.example.explorandes.utils.NetworkResult
import com.example.explorandes.viewmodels.EventViewModel
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView

class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var noConnectionView: View
    private var wasOfflineBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize viewModel using the apiService directly
        val eventRepository = EventRepository(ApiClient.apiService)
        val factory = EventViewModel.Factory(eventRepository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        // Set up the offline view - completely rewritten
        val rootView = binding.root
        
        // Create our own offline view without trying to find it first
        val inflatedOfflineView = layoutInflater.inflate(R.layout.layout_no_connection, null)
        // Set visibility first
        inflatedOfflineView.visibility = View.GONE
        
        // Find and modify TextView (with proper imports)
        val messageTextView = inflatedOfflineView.findViewById<android.widget.TextView>(R.id.no_connection_message)
        if (messageTextView != null) {
            messageTextView.text = "You're offline. Showing cached events."
        }
        
        // Add to parent
        (rootView as? ViewGroup)?.addView(inflatedOfflineView)
        noConnectionView = inflatedOfflineView
        
        // Set up retry button
        val retryButton = inflatedOfflineView.findViewById<Button>(R.id.retry_button)
        retryButton?.setOnClickListener {
            val connected = viewModel.checkConnectivity()
            if (connected) {
                wasOfflineBefore = false
                showContent()
                viewModel.loadEvents()
            } else {
                Toast.makeText(context, "Still no internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        setupSearchView()
        setupRefreshLayout()
        observeViewModel()
        
        // Observe connectivity status
        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                if (wasOfflineBefore) {
                    // Coming back online
                    Snackbar.make(
                        requireView(),
                        "Internet connection restored",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    
                    // Refresh data
                    viewModel.loadEvents()
                    showContent()
                }
                wasOfflineBefore = false
            } else {
                wasOfflineBefore = true
                
                // If we have events, show snackbar
                // Otherwise show the offline view
                if (eventAdapter.itemCount > 0) {
                    Snackbar.make(
                        requireView(),
                        "You're offline. Showing cached events.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") {
                        viewModel.checkConnectivity()
                    }.show()
                } else {
                    showOfflineView()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            navigateToEventDetail(event)
        }

        binding.recyclerViewEvents.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupFilterChips() {
        binding.apply {
            // Set up filter chips
            chipAll.setOnClickListener { viewModel.applyFilter(null) }
            chipEvents.setOnClickListener { viewModel.applyFilter(Event.TYPE_EVENT) }
            chipMovies.setOnClickListener { viewModel.applyFilter(Event.TYPE_MOVIE) }
            chipSports.setOnClickListener { viewModel.applyFilter(Event.TYPE_SPORTS) }
            chipUpcoming.setOnClickListener { viewModel.applyFilter("upcoming") }
            chipOngoing.setOnClickListener { viewModel.applyFilter("ongoing") }

            // Observe current filter to update chip UI
            viewModel.currentFilter.observe(viewLifecycleOwner) { filter ->
                updateChipSelection(filter)
            }
        }
    }

    private fun updateChipSelection(filter: String?) {
        binding.apply {
            val chips = listOf(
                chipAll, chipEvents, chipMovies, chipSports, chipUpcoming, chipOngoing
            )

            chips.forEach { chip ->
                chip.isChecked = false
            }

            when (filter) {
                null -> chipAll.isChecked = true
                Event.TYPE_EVENT -> chipEvents.isChecked = true
                Event.TYPE_MOVIE -> chipMovies.isChecked = true
                Event.TYPE_SPORTS -> chipSports.isChecked = true
                "upcoming" -> chipUpcoming.isChecked = true
                "ongoing" -> chipOngoing.isChecked = true
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        viewModel.searchEvents(it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Only search when user stops typing
                if (newText.isNullOrBlank()) {
                    viewModel.loadEvents()
                }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            viewModel.loadEvents()
            false
        }
    }

    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadEvents()
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE

                    val events = result.data ?: emptyList()
                    eventAdapter.submitList(events)

                    if (events.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.recyclerViewEvents.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.recyclerViewEvents.visibility = View.VISIBLE
                    }
                }
                is NetworkResult.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    
                    // Check if it's a connectivity error
                    if (result.message?.contains("internet") == true || 
                        result.message?.contains("network") == true ||
                        result.message?.contains("connection") == true) {
                        
                        // Show offline view if no data
                        if (eventAdapter.itemCount == 0) {
                            showOfflineView()
                        } else {
                            // Show snackbar if we have data
                            Snackbar.make(
                                requireView(),
                                "You're offline. Showing cached events.",
                                Snackbar.LENGTH_LONG
                            ).setAction("Retry") {
                                viewModel.checkConnectivity()
                            }.show()
                        }
                    } else {
                        // Other errors
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is NetworkResult.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun navigateToEventDetail(event: Event) {
        try {
            // Store the selected event in the ViewModel
            viewModel.setSelectedEvent(event)

            // Check if we're using the Navigation Component (in a fragment container)
            if (findNavController().currentDestination?.id == R.id.eventListFragment) {
                // Use navigation component
                findNavController().navigate(R.id.action_eventListFragment_to_eventDetailFragment)
            } else {
                // Fallback to Activity-based approach
                val intent = Intent(requireContext(), EventDetailActivity::class.java).apply {
                    putExtra("EVENT", event) // Assuming Event is Parcelable
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            // Another fallback if navigation fails
            Toast.makeText(requireContext(),
                "Error navigating to event details: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showOfflineView() {
        binding.swipeRefreshLayout.visibility = View.GONE
        noConnectionView.visibility = View.VISIBLE
    }
    
    private fun showContent() {
        binding.swipeRefreshLayout.visibility = View.VISIBLE
        noConnectionView.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Check connectivity when fragment becomes visible
        viewModel.checkConnectivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = EventListFragment()
    }
}
