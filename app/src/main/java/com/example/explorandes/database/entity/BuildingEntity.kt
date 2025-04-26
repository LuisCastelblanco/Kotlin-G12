package com.example.explorandes.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.explorandes.models.Building
import java.time.LocalDateTime

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val category: String,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    fun toModel(): Building {
        return Building(
            id = id,
            name = name,
            code = code,
            description = description,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl,
            category = category
        )
    }

    companion object {
        fun fromModel(building: Building): BuildingEntity {
            return BuildingEntity(
                id = building.id,
                name = building.name,
                code = building.code,
                description = building.description,
                latitude = building.latitude,
                longitude = building.longitude,
                imageUrl = building.imageUrl,
                category = building.category
            )
        }
    }
}