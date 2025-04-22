package com.example.explorandes.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.explorandes.MapActivity
import com.example.explorandes.R
import com.example.explorandes.models.Place

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

        // Configure item click
        holder.itemView.setOnClickListener {
            onPlaceClick(place)
        }
        
        // Configure navigation button
        holder.itemView.findViewById<Button>(R.id.btn_navigate).setOnClickListener {
            // First get buildingId from the place
            val buildingId = place.building?.id ?: place.buildingId
            
            // If we have a buildingId, start the MapActivity
            if (buildingId != null) {
                val context = holder.itemView.context
                val intent = Intent(context, MapActivity::class.java)
                intent.putExtra("BUILDING_ID", buildingId)
                context.startActivity(intent)
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