package com.example.explorandes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.explorandes.ui.map.MapFragment

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Get building ID from intent
        val buildingId = intent.getLongExtra("BUILDING_ID", -1L)
        
        if (savedInstanceState == null) {
            if (buildingId != -1L) {
                // Create map fragment with building ID
                val mapFragment = MapFragment.newInstance(buildingId)
                
                // Add the fragment to the activity
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit()
            } else {
                // If no building ID, just show the map
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map_container, MapFragment())
                    .commit()
            }
        }
    }
}