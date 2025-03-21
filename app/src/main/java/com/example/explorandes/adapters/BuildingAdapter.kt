package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.R
import com.example.explorandes.models.Building

class BuildingAdapter(
    private val buildings: List<Building>,
    private val onBuildingClickListener: (Building) -> Unit = {}
) : RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder>() {

    class BuildingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buildingImage: ImageView = itemView.findViewById(R.id.building_image)
        val buildingName: TextView = itemView.findViewById(R.id.building_name)
        val buildingLocation: TextView = itemView.findViewById(R.id.building_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_building, parent, false)
        return BuildingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        val building = buildings[position]
        holder.buildingName.text = building.name
        holder.buildingLocation.text = building.location
        holder.buildingImage.setImageResource(building.imageResId)
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onBuildingClickListener(building)
        }
    }

    override fun getItemCount() = buildings.size
}