package com.example.explorandes

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.explorandes.models.Event
import com.google.android.material.appbar.MaterialToolbar

class EventDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val eventImage: ImageView = findViewById(R.id.event_image)
        val eventTitle: TextView = findViewById(R.id.event_title)
        val eventTime: TextView = findViewById(R.id.event_time)
        val eventLocation: TextView = findViewById(R.id.event_location)
        val eventDescription: TextView = findViewById(R.id.event_description)

        val event = intent.getParcelableExtra<Event>("EVENT")
        if (event == null) {
            Toast.makeText(this, "No se encontró información del evento", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        eventTitle.text = event.title
        eventTime.text = event.getFormattedTimeRange()
        eventLocation.text = event.locationName ?: "Ubicación no disponible"
        eventDescription.text = event.description ?: "Sin descripción"

        Glide.with(this)
            .load(event.imageUrl)
            .placeholder(R.drawable.placeholder_event)
            .into(eventImage)
    }
}
