package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.models.Building

class BuildingAdapter(
    private var buildings: List<Building>,
    private val onBuildingClicked: (Building) -> Unit
) : RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder>() {

    class BuildingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.building_image)
        val name: TextView = view.findViewById(R.id.building_name)
        val code: TextView = view.findViewById(R.id.building_code)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_building, parent, false)
        return BuildingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        val building = buildings[position]

        holder.name.text = building.name
        holder.code.text = building.code

        // Load image
        if (!building.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.image.context)
                .load(building.imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.profile_placeholder)
        }

        // Set click listener on the entire item
        holder.itemView.setOnClickListener {
            onBuildingClicked(building)
        }
    }

    override fun getItemCount() = buildings.size

    fun updateData(newBuildings: List<Building>) {
        buildings = newBuildings
        notifyDataSetChanged()
    }
}