package com.example.explorandes.adapters

import android.content.Intent
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.explorandes.MapActivity
import com.example.explorandes.R
import com.example.explorandes.models.Place
import com.example.explorandes.utils.CustomImageCache
import com.example.explorandes.utils.ImageDiskCache

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

        // Load image with caching strategy
        val imageUrl = place.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            // Obtener el contexto para inicializar ImageDiskCache
            val context = holder.itemView.context
            val imageDiskCache = ImageDiskCache.getInstance(context)
            
            // 1. Primero, intentar obtener de la caché en memoria
            val cachedBitmap = CustomImageCache.getBitmapFromCache(imageUrl)

            if (cachedBitmap != null) {
                // Si está en la caché en memoria, usarlo directamente
                holder.placeImage.setImageBitmap(cachedBitmap)
            } else {
                // 2. Si no está en memoria, intentar obtener de la caché en disco
                val diskCachedBitmap = imageDiskCache.loadBitmapFromDisk(imageUrl)
                
                if (diskCachedBitmap != null) {
                    // Si está en la caché en disco, usarlo y guardarlo en memoria
                    holder.placeImage.setImageBitmap(diskCachedBitmap)
                    CustomImageCache.putBitmapInCache(imageUrl, diskCachedBitmap)
                } else {
                    // 3. Si no está en ninguna caché, descargarlo con Glide
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .load(imageUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(object : CustomTarget<android.graphics.Bitmap>() {
                            override fun onResourceReady(resource: android.graphics.Bitmap, transition: Transition<in android.graphics.Bitmap>?) {
                                // Mostrar la imagen
                                holder.placeImage.setImageBitmap(resource)
                                
                                // Guardar en ambas cachés
                                CustomImageCache.putBitmapInCache(imageUrl, resource)
                                imageDiskCache.saveBitmapToDisk(imageUrl, resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                holder.placeImage.setImageDrawable(placeholder)
                            }
                        })
                }
            }
        } else {
            holder.placeImage.setImageResource(R.drawable.profile_placeholder)
        }

        // Configure item click
        holder.itemView.setOnClickListener {
            onPlaceClick(place)
        }
        
//        // Configure navigation button
//        holder.itemView.findViewById<Button>(R.id.btn_navigate).setOnClickListener {
//            // First get buildingId from the place
//            val buildingId = place.building?.id ?: place.buildingId
//
//            // If we have a buildingId, start the MapActivity
//            if (buildingId != null) {
//                val context = holder.itemView.context
//                val intent = Intent(context, MapActivity::class.java)
//                intent.putExtra("BUILDING_ID", buildingId)
//                context.startActivity(intent)
//            }
//        }
    }

    override fun getItemCount() = places.size

    // Method to update data when fetched from backend
    fun updateData(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}