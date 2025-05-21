package com.example.explorandes.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.databinding.FragmentEventDetailBinding
import com.example.explorandes.models.EventDetail
import com.example.explorandes.utils.NetworkResult
import com.example.explorandes.viewmodels.EventDetailViewModel
import com.example.explorandes.viewmodels.EventViewModel
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventDetailViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    
    private var eventId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener ID del evento de los argumentos
        arguments?.let {
            eventId = it.getLong("eventId", -1)
        }

        // Inicializar ViewModel
        viewModel = ViewModelProvider(
            this,
            EventDetailViewModel.Factory(requireContext())
        )[EventDetailViewModel::class.java]
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

        setupUI()
        setupObservers()
        
        // Cargar datos del evento
        if (eventId != -1L) {
            viewModel.loadEventDetail(eventId)
        } else {
            // Si no hay ID, buscar evento seleccionado en ViewModel compartido
            val sharedViewModel = ViewModelProvider(requireActivity())[EventViewModel::class.java]
            sharedViewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
                if (event != null) {
                    eventId = event.id
                    viewModel.loadEventDetail(eventId)
                }
            }
        }
        
        // Verificar si hay eventos pendientes de sincronización
        viewModel.checkConnectivity()
        if (viewModel.isConnected.value == true) {
            viewModel.syncPendingEvents()
        }
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configurar SwipeRefresh
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            if (eventId != -1L) {
                viewModel.loadEventDetail(eventId)
            }
        }
        
        // Botón de compartir
        binding.fabShare.setOnClickListener {
            val eventDetail = (viewModel.eventDetail.value as? NetworkResult.Success)?.data
            eventDetail?.let { shareEvent(it) }
        }
        
        // Botón de agregar al calendario
        binding.fabAddToCalendar.setOnClickListener {
            val eventDetail = (viewModel.eventDetail.value as? NetworkResult.Success)?.data
            eventDetail?.let { addEventToCalendar(it) }
        }
        
        // Mostrar ubicación
        binding.locationLayout.setOnClickListener {
            val eventDetail = (viewModel.eventDetail.value as? NetworkResult.Success)?.data
            eventDetail?.locationId?.let { locationId ->
                navigateToLocation(locationId)
            }
        }
    }
    
    private fun setupObservers() {
        // Observar estado de conectividad
        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (!isConnected) {
                Snackbar.make(
                    binding.root,
                    "Sin conexión. Mostrando datos almacenados localmente.",
                    Snackbar.LENGTH_LONG
                ).setAction("Reintentar") {
                    viewModel.checkConnectivity()
                }.show()
            }
        }
        
        // Observar datos del evento
        viewModel.eventDetail.observe(viewLifecycleOwner) { result ->
            swipeRefreshLayout.isRefreshing = false
            
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { updateUI(it) }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.data != null) {
                        // Tenemos datos en caché pero hubo error al actualizar
                        updateUI(result.data)
                        Snackbar.make(
                            binding.root,
                            "Error al actualizar: ${result.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        // No hay datos en caché ni se pudo cargar de la red
                        showErrorView(result.message ?: "Error desconocido")
                    }
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        
        // Observar estado de sincronización
        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                "pending_sync" -> {
                    Snackbar.make(
                        binding.root,
                        "Cambios guardados localmente, pendientes de sincronización",
                        Snackbar.LENGTH_LONG
                    ).setAction("Sincronizar") {
                        if (viewModel.checkConnectivity()) {
                            viewModel.syncPendingEvents()
                        }
                    }.show()
                }
                "synced" -> {
                    Snackbar.make(
                        binding.root,
                        "Cambios sincronizados correctamente",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                "sync_error" -> {
                    Snackbar.make(
                        binding.root,
                        "Error al sincronizar cambios",
                        Snackbar.LENGTH_LONG
                    ).setAction("Reintentar") {
                        if (viewModel.checkConnectivity()) {
                            viewModel.syncPendingEvents()
                        }
                    }.show()
                }
                "sync_completed" -> {
                    Snackbar.make(
                        binding.root,
                        "Sincronización completada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUI(eventDetail: EventDetail) {
        binding.apply {
            toolbarLayout.title = eventDetail.title

            // Cargar imagen del evento
            eventDetail.imageUrl?.let { url ->
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .centerCrop()
                    .into(eventImage)
            } ?: run {
                eventImage.setImageResource(R.drawable.placeholder_event)
            }

            // Establecer descripción
            if (!eventDetail.description.isNullOrBlank()) {
                tvDescription.text = eventDetail.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // Establecer fecha y hora
            tvDate.text = eventDetail.getFormattedDate()
            tvTime.text = eventDetail.getFormattedTimeRange()

            // Establecer ubicación si está disponible
            if (eventDetail.locationName != null) {
                tvLocation.text = eventDetail.locationName
                locationLayout.visibility = View.VISIBLE
            } else {
                locationLayout.visibility = View.GONE
            }

            // Establecer tipo de evento
            when (eventDetail.type) {
                "event" -> {
                    tvEventType.text = "Event"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                }
                "movies" -> {
                    tvEventType.text = "Movie"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_movie)
                }
                "sports" -> {
                    tvEventType.text = "Sports"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_sports)
                }
                else -> {
                    tvEventType.text = "Event"
                    tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                }
            }

            // Resaltar "Now Playing" para eventos en curso
            if (eventDetail.isHappeningNow()) {
                tvNowPlaying.visibility = View.VISIBLE
            } else {
                tvNowPlaying.visibility = View.GONE
            }

            // Mostrar "Add to Calendar" solo para eventos futuros
            if (eventDetail.isUpcoming()) {
                fabAddToCalendar.visibility = View.VISIBLE
            } else {
                fabAddToCalendar.visibility = View.GONE
            }
            
            // Mostrar información adicional si está disponible
            if (!eventDetail.additionalInfo.isNullOrBlank()) {
                additionalInfoCard.visibility = View.VISIBLE
                tvAdditionalInfo.text = eventDetail.additionalInfo
            } else {
                additionalInfoCard.visibility = View.GONE
            }
            
            // Mostrar información del organizador si está disponible
            if (!eventDetail.organizerName.isNullOrBlank()) {
                organizerLayout.visibility = View.VISIBLE
                tvOrganizerName.text = eventDetail.organizerName
            } else {
                organizerLayout.visibility = View.GONE
            }
            
            // Mostrar capacidad si está disponible
            if (eventDetail.capacity != null) {
                capacityLayout.visibility = View.VISIBLE
                tvCapacity.text = "${eventDetail.capacity} personas"
            } else {
                capacityLayout.visibility = View.GONE
            }
            
            // Mostrar URL de registro si está disponible
            if (!eventDetail.registrationUrl.isNullOrBlank()) {
                btnRegister.visibility = View.VISIBLE
                btnRegister.setOnClickListener {
                    // Aquí se podría abrir el navegador con la URL de registro
                    Toast.makeText(context, "Regístrate en: ${eventDetail.registrationUrl}", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnRegister.visibility = View.GONE
            }
        }
    }

    private fun showErrorView(message: String) {
        binding.apply {
            progressBar.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            // Usando el ID correcto que establecimos en el XML
            tvErrorMessage.text = message

            btnRetry.setOnClickListener {
                errorLayout.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                viewModel.loadEventDetail(eventId)
            }
        }
    }

    private fun navigateToLocation(locationId: Long) {
        val bundle = Bundle().apply {
            putLong("buildingId", locationId)
        }
        findNavController().navigate(R.id.action_eventDetailFragment_to_buildingDetailFragment, bundle)
    }

    private fun addEventToCalendar(eventDetail: EventDetail) {
        val startMillis = LocalDateTime.parse(
            eventDetail.startTime,
            DateTimeFormatter.ISO_DATE_TIME
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val endMillis = LocalDateTime.parse(
            eventDetail.endTime,
            DateTimeFormatter.ISO_DATE_TIME
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, eventDetail.title)
            putExtra(CalendarContract.Events.DESCRIPTION, eventDetail.description)
            eventDetail.locationName?.let {
                putExtra(CalendarContract.Events.EVENT_LOCATION, it)
            }
        }

        startActivity(intent)
    }

    private fun shareEvent(eventDetail: EventDetail) {
        val shareText = """
            Event: ${eventDetail.title}
            Date: ${eventDetail.getFormattedDate()}
            Time: ${eventDetail.getFormattedTimeRange()}
            ${if (eventDetail.locationName != null) "Location: ${eventDetail.locationName}" else ""}
            
            ${eventDetail.description ?: ""}
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