package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.models.Building

class BuildingAdapter(
    private var buildings: List<Building>,
    private val onBuildingClick: (Building) -> Unit = {}
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
        holder.buildingLocation.text = building.description ?: ""

        // Load image from URL instead of resource
        if (!building.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(building.imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.buildingImage)
        } else {
            holder.buildingImage.setImageResource(R.drawable.profile_placeholder)
        }

        holder.itemView.setOnClickListener {
            onBuildingClick(building)
        }
    }

    override fun getItemCount() = buildings.size

    // Method to update data when fetched from backend
    fun updateData(newBuildings: List<Building>) {
        buildings = newBuildings
        notifyDataSetChanged()
    }
}