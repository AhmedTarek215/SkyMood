package com.example.skymood.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val countryName: String,
    val lat: Double,
    val lon: Double
)
