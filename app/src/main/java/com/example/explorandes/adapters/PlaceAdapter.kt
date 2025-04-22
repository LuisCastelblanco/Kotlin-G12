package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.models.Place
import android.widget.Button
import android.os.Bundle
import androidx.fragment.app.MapFragment
import androidx.fragment.app.FragmentActivity

class PlaceAdapter(
    private var places: List<Place>,
    private val onPlaceClick: (Place) -> Unit = {}
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeImage: ImageView = itemView.findViewById(R.id.place_image)
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val placeLocation: TextView = itemView.findViewById(R.id.place_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }


    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.name
        holder.placeLocation.text = place.floor ?: ""

        // Load image from URL
        if (!place.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(place.imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.placeImage)
        } else {
            holder.placeImage.setImageResource(R.drawable.profile_placeholder)
        }

        // Configura el click en el ítem completo
        holder.itemView.setOnClickListener {
            onPlaceClick(place)
        }
        
        // Configura el botón de navegación
        holder.itemView.findViewById<Button>(R.id.btn_navigate).setOnClickListener {
            // Primero obtener el buildingId desde el place
            val buildingId = place.building?.id ?: place.buildingId
            
            // Si tenemos un buildingId, iniciamos el fragmento de mapa
            if (buildingId != null) {
                val mapFragment = MapFragment().apply {
                    arguments = Bundle().apply {
                        putLong("BUILDING_ID", buildingId)
                    }
                }
                
                // Obtenemos el fragmentManager y realizamos la transacción
                val fragmentManager = (holder.itemView.context as? FragmentActivity)?.supportFragmentManager
                fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, mapFragment)
                    ?.addToBackStack("map")
                    ?.commit()
            }
        }
    }
    override fun getItemCount() = places.size

    // Method to update data when fetched from backend
    fun updateData(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}