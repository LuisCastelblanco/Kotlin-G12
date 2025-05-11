package com.example.explorandes.database.dao

import androidx.room.*
import com.example.explorandes.database.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE id = :id")
    suspend fun getBuildingById(id: Long): BuildingEntity?

    @Query("SELECT * FROM buildings WHERE category = :category")
    fun getBuildingsByCategory(category: String): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildings(buildings: List<BuildingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    @Delete
    suspend fun deleteBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings")
    suspend fun deleteAllBuildings()

    @Query("SELECT * FROM buildings WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchBuildings(query: String): Flow<List<BuildingEntity>>
}