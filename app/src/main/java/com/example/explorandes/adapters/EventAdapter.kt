package com.example.explorandes.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.explorandes.R
import com.example.explorandes.databinding.ItemEventBinding
import com.example.explorandes.models.Event
import com.example.explorandes.utils.CustomImageCache
import com.example.explorandes.utils.ImageDiskCache

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEventClick(getItem(position))
                }
            }
        }

        fun bind(event: Event) {
            binding.apply {
                tvEventTitle.text = event.title
                tvEventDate.text = event.getFormattedDate()
                tvEventTime.text = event.getFormattedTimeRange()

                event.locationName?.let {
                    tvEventLocation.text = it
                    tvEventLocation.visibility = View.VISIBLE
                } ?: run {
                    tvEventLocation.visibility = View.GONE
                }

                when (event.type) {
                    Event.TYPE_EVENT -> {
                        tvEventType.text = "Event"
                        tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                    }
                    Event.TYPE_MOVIE -> {
                        tvEventType.text = "Movie"
                        tvEventType.setBackgroundResource(R.drawable.bg_badge_movie)
                    }
                    Event.TYPE_SPORTS -> {
                        tvEventType.text = "Sports"
                        tvEventType.setBackgroundResource(R.drawable.bg_badge_sports)
                    }
                    else -> {
                        tvEventType.text = "Event"
                        tvEventType.setBackgroundResource(R.drawable.bg_badge_event)
                    }
                }

                if (event.isHappeningNow()) {
                    cardEvent.strokeWidth = 2
                    cardEvent.strokeColor = root.context.getColor(R.color.colorAccent)
                    tvNowPlaying.visibility = View.VISIBLE
                } else {
                    cardEvent.strokeWidth = 0
                    tvNowPlaying.visibility = View.GONE
                }

                val imageUrl = event.imageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    // Obtener el contexto para inicializar ImageDiskCache
                    val context = root.context
                    val imageDiskCache = ImageDiskCache.getInstance(context)
                    
                    // 1. Primero, intentar obtener de la caché en memoria
                    val cachedBitmap = CustomImageCache.getBitmapFromCache(imageUrl)

                    if (cachedBitmap != null) {
                        // Si está en la caché en memoria, usarlo directamente
                        ivEventImage.setImageBitmap(cachedBitmap)
                    } else {
                        // 2. Si no está en memoria, intentar obtener de la caché en disco
                        val diskCachedBitmap = imageDiskCache.loadBitmapFromDisk(imageUrl)
                        
                        if (diskCachedBitmap != null) {
                            // Si está en la caché en disco, usarlo y guardarlo en memoria
                            ivEventImage.setImageBitmap(diskCachedBitmap)
                            CustomImageCache.putBitmapInCache(imageUrl, diskCachedBitmap)
                        } else {
                            // 3. Si no está en ninguna caché, descargarlo con Glide
                            Glide.with(ivEventImage.context)
                                .asBitmap()
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_event)
                                .error(R.drawable.placeholder_event)
                                .centerCrop()
                                .into(object : CustomTarget<android.graphics.Bitmap>() {
                                    override fun onResourceReady(resource: android.graphics.Bitmap, transition: Transition<in android.graphics.Bitmap>?) {
                                        // Mostrar la imagen
                                        ivEventImage.setImageBitmap(resource)
                                        
                                        // Guardar en ambas cachés
                                        CustomImageCache.putBitmapInCache(imageUrl, resource)
                                        imageDiskCache.saveBitmapToDisk(imageUrl, resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        ivEventImage.setImageDrawable(placeholder)
                                    }
                                })
                        }
                    }
                } else {
                    ivEventImage.setImageResource(R.drawable.placeholder_event)
                }
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}