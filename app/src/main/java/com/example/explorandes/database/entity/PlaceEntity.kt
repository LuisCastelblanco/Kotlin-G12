package com.example.explorandes.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.explorandes.models.Place
import java.time.LocalDateTime

@Entity(
    tableName = "places",
    foreignKeys = [
        ForeignKey(
            entity = BuildingEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class PlaceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val code: String?,
    val category: String?,
    val floor: String?,
    val coordinates: String?,
    val imageUrl: String?,
    val buildingId: Long?,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    fun toModel(): Place {
        return Place(
            id = id,
            name = name,
            code = code,
            category = category,
            floor = floor,
            coordinates = coordinates,
            imageUrl = imageUrl,
            buildingId = buildingId
        )
    }

    companion object {
        fun fromModel(place: Place): PlaceEntity {
            return PlaceEntity(
                id = place.id,
                name = place.name,
                code = place.code,
                category = place.category,
                floor = place.floor,
                coordinates = place.coordinates,
                imageUrl = place.imageUrl,
                buildingId = place.getEffectiveBuildingId()
            )
        }
    }
}