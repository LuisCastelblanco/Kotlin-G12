package com.example.explorandes.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.explorandes.models.Event
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val type: String?,
    val startTime: String,
    val endTime: String,
    val locationId: Long?,
    val locationName: String?,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    fun toModel(): Event {
        return Event(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            type = type,
            startTime = startTime,
            endTime = endTime,
            locationId = locationId,
            locationName = locationName
        )
    }

    companion object {
        fun fromModel(event: Event): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                description = event.description,
                imageUrl = event.imageUrl,
                type = event.type,
                startTime = event.startTime,
                endTime = event.endTime,
                locationId = event.locationId,
                locationName = event.locationName
            )
        }
    }
}