package com.example.explorandes.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.explorandes.R
import com.example.explorandes.models.Building
import com.example.explorandes.utils.CustomImageCache

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

        val imageUrl = building.imageUrl

        if (!imageUrl.isNullOrEmpty()) {
            val cachedBitmap = CustomImageCache.getBitmapFromCache(imageUrl)

            if (cachedBitmap != null) {
                holder.image.setImageBitmap(cachedBitmap)
            } else {
                Glide.with(holder.image.context)
                    .asBitmap()
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(object : CustomTarget<android.graphics.Bitmap>() {
                        override fun onResourceReady(resource: android.graphics.Bitmap, transition: Transition<in android.graphics.Bitmap>?) {
                            holder.image.setImageBitmap(resource)
                            CustomImageCache.putBitmapInCache(imageUrl, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            holder.image.setImageDrawable(placeholder)
                        }
                    })
            }
        } else {
            holder.image.setImageResource(R.drawable.profile_placeholder)
        }

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