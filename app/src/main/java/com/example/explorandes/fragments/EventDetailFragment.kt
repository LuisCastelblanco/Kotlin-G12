package com.example.explorandes.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.api.ApiClient
import com.example.explorandes.databinding.FragmentEventDetailBinding
import com.example.explorandes.models.Event
import com.example.explorandes.repositories.EventRepository
import com.example.explorandes.viewmodels.EventViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize viewModel using standard pattern (no Hilt)
        val eventRepository = EventRepository.getInstance(
            ApiClient.retrofit // Access retrofit directly from ApiClient
        )
        val factory = EventViewModel.Factory(eventRepository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
            updateUI(event)
        }
    }

    private fun updateUI(event: Event) {
        binding.apply {
            toolbarLayout.title = event.title

            // Load event image
            event.imageUrl?.let { url ->
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .centerCrop()
                    .into(eventImage)
            } ?: run {
                eventImage.setImageResource(R.drawable.placeholder_event)
            }

            // Set description
            if (!event.description.isNullOrBlank()) {
                tvDescription.text = event.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // Set date and time
            tvDate.text = event.getFormattedDate()
            tvTime.text = event.getFormattedTimeRange()

            // Set location if available
            if (event.locationName != null) {
                tvLocation.text = event.locationName
                locationLayout.visibility = View.VISIBLE

                // Navigate to the location when clicked
                locationLayout.setOnClickListener {
                    event.locationId?.let { locationId ->
                        navigateToLocation(locationId)
                    }
                }
            } else {
                locationLayout.visibility = View.GONE
            }

            // Set event type badge
            when (event.type) {
                Event.TYPE_EVENT -> {
                    tvEventType.text = "Event"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                }
                Event.TYPE_MOVIE -> {
                    tvEventType.text = "Movie"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_movie)
                }
                Event.TYPE_SPORTS -> {
                    tvEventType.text = "Sports"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_sports)
                }
                else -> {
                    tvEventType.text = "Event"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                }
            }

            // Highlight "Now Playing" for ongoing events
            if (event.isHappeningNow()) {
                tvNowPlaying.visibility = View.VISIBLE
            } else {
                tvNowPlaying.visibility = View.GONE
            }

            // Set up the "Add to Calendar" button
            fabAddToCalendar.setOnClickListener {
                addEventToCalendar(event)
            }

            // Only show "Add to Calendar" for upcoming events
            if (event.isUpcoming()) {
                fabAddToCalendar.visibility = View.VISIBLE
            } else {
                fabAddToCalendar.visibility = View.GONE
            }

            // Share event
            fabShare.setOnClickListener {
                shareEvent(event)
            }
        }
    }

    private fun navigateToLocation(locationId: Long) {
        val bundle = Bundle().apply {
            putLong("buildingId", locationId)
        }
        findNavController().navigate(R.id.action_eventDetailFragment_to_buildingDetailFragment, bundle)
    }

    private fun addEventToCalendar(event: Event) {
        val startMillis = LocalDateTime.parse(
            event.startTime,
            DateTimeFormatter.ISO_DATE_TIME
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val endMillis = LocalDateTime.parse(
            event.endTime,
            DateTimeFormatter.ISO_DATE_TIME
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            event.locationName?.let {
                putExtra(CalendarContract.Events.EVENT_LOCATION, it)
            }
        }

        startActivity(intent)
    }

    private fun shareEvent(event: Event) {
        val shareText = """
            Event: ${event.title}
            Date: ${event.getFormattedDate()}
            Time: ${event.getFormattedTimeRange()}
            ${if (event.locationName != null) "Location: ${event.locationName}" else ""}
            
            ${event.description ?: ""}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Event")
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}