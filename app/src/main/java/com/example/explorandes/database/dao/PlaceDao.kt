package com.example.explorandes.database.dao

import androidx.room.*
import com.example.explorandes.database.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Long): PlaceEntity?

    @Query("SELECT * FROM places WHERE buildingId = :buildingId")
    fun getPlacesByBuilding(buildingId: Long): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category = :category")
    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%'")
    fun searchPlaces(query: String): Flow<List<PlaceEntity>>
}