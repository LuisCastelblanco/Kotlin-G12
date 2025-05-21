package com.example.explorandes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.explorandes.database.dao.BuildingDao
import com.example.explorandes.database.dao.EventDao
import com.example.explorandes.database.dao.EventDetailDao
import com.example.explorandes.database.dao.PlaceDao
import com.example.explorandes.database.entity.BuildingEntity
import com.example.explorandes.database.entity.EventDetailEntity
import com.example.explorandes.database.entity.EventEntity
import com.example.explorandes.database.entity.PlaceEntity
import com.example.explorandes.database.typeconverters.DateConverter

@Database(
    entities = [
        BuildingEntity::class,
        PlaceEntity::class,
        EventEntity::class,
        EventDetailEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun buildingDao(): BuildingDao
    abstract fun placeDao(): PlaceDao
    abstract fun eventDao(): EventDao
    abstract fun eventDetailDao(): EventDetailDao

    companion object {
        private const val DATABASE_NAME = "explorandes_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}