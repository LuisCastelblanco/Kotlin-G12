package com.example.explorandes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorandes.R
import com.example.explorandes.adapters.EventAdapter
import com.example.explorandes.api.ApiClient
import com.example.explorandes.databinding.FragmentEventListBinding
import com.example.explorandes.models.Event
import com.example.explorandes.repositories.EventRepository
import com.example.explorandes.utils.NetworkResult
import com.example.explorandes.viewmodels.EventViewModel

class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a simple repository that uses ApiClient.apiService directly
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

        setupRecyclerView()
        setupFilterChips()
        setupSearchView()
        setupRefreshLayout()
        observeViewModel()
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
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
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
        viewModel.setSelectedEvent(event)
        findNavController().navigate(R.id.action_eventListFragment_to_eventDetailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // Add to EventListFragment.kt
    companion object {
        fun newInstance() = EventListFragment()
    }
}